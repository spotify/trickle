/*
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.trickle;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.Fallbacks.always;
import static com.spotify.trickle.Trickle.call;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Integration-level Trickle tests.
 */
public class TrickleTest {
  Func0<String> node1;

  SettableFuture<String> future1;
  ListeningExecutorService executorService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private Input<String> debugInfoInput;
  private Graph<Integer> debugInfoLength;
  private Graph<String> debugInfoReport;

  @Before
  public void setUp() throws Exception {
    future1 = SettableFuture.create();

    node1 = new Func0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return future1;
      }
    };
    executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    debugInfoInput = Input.named("weirdName");
  }

  @After
  public void shutdown() {
    executorService.shutdown();
  }

  @Test
  public void shouldConstructSingleNodeGraph() throws Exception {
    Graph<String> graph = call(node1);

    ListenableFuture<String> actual = graph.run();
    future1.set("hello world!!");

    assertThat(actual.get(), equalTo("hello world!!"));
  }

  @Test
  public void shouldExecuteSingleNodeAsynchronously() throws Exception {
    Graph<String> graph = call(node1);

    ListenableFuture<String> actual = graph.run();

    assertThat(actual.isDone(), is(false));

    future1.set("ok, done");
    assertThat(actual.isDone(), is(true));
  }

  @Test
  public void shouldUseInputs() throws Exception {
    Func1<String, String> node = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String name) {
        return immediateFuture("hello " + name + "!");
      }
    };

    Input<String> input = Input.named("theInnnput");
    Graph<String> graph = call(node).with(input);

    ListenableFuture<String> future = graph.bind(input, "petter").run();
    assertThat(future.get(), equalTo("hello petter!"));
  }

  @Test
  public void shouldCallDependenciesOnlyOnce() throws Exception {
    final AtomicInteger counter = new AtomicInteger(0);

    Func1<String, String> greet = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String name) {
        counter.incrementAndGet();
        return immediateFuture("hello " + name + "!");
      }
    };
    Func1<String, String> noop = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String input) {
        return immediateFuture(input);
      }
    };
    Func2<String, String, Integer> node2 = new Func2<String, String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String input1, String input2) {
        return immediateFuture(input1.length() + input2.length());
      }
    };

    Input<String> input = Input.named("theInnnput");
    Graph<String> g1 = call(greet).with(input).named("111");
    Graph<String> g2 = call(noop).with(g1).named("222");
    Graph<Integer> g3 = call(node2).with(g2, g1).named("333");

    ListenableFuture<Integer> future = g3.bind(input, "rouz").run();
    assertThat(future.get(), equalTo(22));
    assertThat(counter.get(), equalTo(1));
  }

  @Test
  public void shouldCallMultipleGraphsOfSameNodeOnceEach() throws Exception {
    final AtomicInteger counter = new AtomicInteger(0);

    Func1<String, String> greet = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String name) {
        counter.incrementAndGet();
        return immediateFuture("hello " + name + "!");
      }
    };
    Func1<String, String> noop = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String input) {
        return immediateFuture(input);
      }
    };
    Func2<String, String, String> node2 = new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String input1, String input2) {
        return immediateFuture(input1 + input2);
      }
    };

    Input<String> input = Input.named("theInnnput");
    Graph<String> g11 = call(greet).with(input).named("1111");
    Graph<String> g12 = call(greet).with(input).named("1112");
    Graph<String> g2 = call(noop).with(g11).named("222");
    Graph<String> g3 = call(node2).with(g2, g11).named("222");
    Graph<String> g4 = call(node2).with(g3, g12).named("333");

    ListenableFuture<String> future = g4.bind(input, "rouz").run();
    assertThat(future.get(), equalTo("hello rouz!hello rouz!hello rouz!"));
    assertThat(counter.get(), equalTo(2));
  }

  @Test
  public void shouldMakeAfterHappenAfter() throws Exception {
    final AtomicInteger counter = new AtomicInteger(0);
    final CountDownLatch latch = new CountDownLatch(1);

    Func0<Void> incr1 = new Func0<Void>() {
      @Override
      public ListenableFuture<Void> run() {
        counter.incrementAndGet();
        return immediateFuture(null);
      }
    };
    Func0<Void> incr2 = new Func0<Void>() {
      @Override
      public ListenableFuture<Void> run() {
        return executorService.submit(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            latch.await();
            counter.incrementAndGet();
            return null;
          }
        });
      }
    };
    Func0<Integer> result = new Func0<Integer>() {
      @Override
      public ListenableFuture<Integer> run() {
        return immediateFuture(counter.get());
      }
    };

    Graph<Void> g1 = call(incr1);
    Graph<Void> g2 = call(incr2).after(g1);
    Graph<Integer> graph = call(result).after(g1, g2);

    ListenableFuture<Integer> future = graph.run();

    assertThat(future.isDone(), is(false));
    assertThat(counter.get(), equalTo(1));

    latch.countDown();

    assertThat(future.get(), equalTo(2));
  }

  @Test
  public void shouldForwardValues() throws Exception {
    Func0<String> first = new Func0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return immediateFuture("hi there!");
      }
    };
    Func1<String, Integer> second = new Func1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String arg) {
        return immediateFuture(arg.length());
      }
    };

    Graph<String> g1 = call(first);
    Graph<Integer> graph = call(second).with(g1);

    assertThat(graph.run().get(), equalTo("hi there!".length()));
  }

  @Test
  public void shouldReturnDefaultForFailedCallWithDefault() throws Exception {
    Func0<String> node = new Func0<String>() {
      @Override
      public ListenableFuture<String> run() {
        throw new RuntimeException("expected");
      }
    };

    Graph<String> graph = call(node).fallback(always("fallback response"));

    assertThat(graph.run(executorService).get(), equalTo("fallback response"));
  }

  @Test
  public void shouldReturnDefaultForFailedCallWithDefaultIntermediateNode() throws Exception {
    Func0<String> node1 = new Func0<String>() {
      @Override
      public ListenableFuture<String> run() {
        throw new RuntimeException("expected");
      }
    };
    Func1<String, Integer> node2 = new Func1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String arg) {
        return immediateFuture(arg.hashCode());
      }
    };

    Graph<String> g1 = call(node1).fallback(always("fallback response"));
    Graph<Integer> graph = call(node2).with(g1);

    assertThat(graph.run(executorService).get(), equalTo("fallback response".hashCode()));
  }

  @Test
  public void shouldReturnDefaultForFailedResponseWithDefault() throws Exception {
    Func0<String> node = new Func0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return immediateFailedFuture(new RuntimeException("expected"));
      }
    };

    Graph<String> graph = call(node).fallback(always("fallback response"));

    assertThat(graph.run(executorService).get(), equalTo("fallback response"));
  }

  @Test
  public void shouldHandleTwoInputParameters() throws Exception {
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };
    Func2<String, String, String> node2 = new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg1, String arg2) {
        return immediateFuture(arg1 + ", " + arg2 + ", 2");
      }
    };

    Input<String> input = Input.named("in");

    Graph<String> g1 = call(node1).with(input);
    Graph<String> g = call(node2).with(g1, input);

    String result = g.bind(input, "hey").run().get();

    assertThat(result, equalTo("hey, 1, hey, 2"));
  }

  @Test
  public void shouldHandleThreeInputParameters() throws Exception {
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };
    Func3<String, String, String, String> node2 = new Func3<String, String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg1, String arg2, String arg3) {
        return immediateFuture(arg1 + ", " + arg2 + ", " + arg3 + ", 2");
      }
    };

    Input<String> input = Input.named("in");
    Input<String> input1 = Input.named("innn");

    Graph<String> g1 = call(node1).with(input);
    Graph<String> g = call(node2).with(g1, input, input1);

    String result = g
        .bind(input, "hey")
        .bind(input1, "ho")
        .run().get();

    assertThat(result, equalTo("hey, 1, hey, ho, 2"));
  }

  @Test
  public void shouldHandleFourInputParameters() throws Exception {
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };
    Func4<String, String, String, String, String> node2 = new Func4<String, String, String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg1, String arg2, String arg3, String arg4) {
        return immediateFuture(arg1 + ", " + arg2 + ", " + arg3 + ", " + arg4 + ", 2");
      }
    };

    Input<String> input = Input.named("in");
    Input<String> input1 = Input.named("innn");
    Input<String> input2 = Input.named("and in again");

    Graph<String> g1 = call(node1).with(input);
    Graph<String> g = call(node2).with(g1, input, input1, input2);

    String result = g
        .bind(input, "hey")
        .bind(input1, "ho")
        .bind(input2, "hum")
        .run().get();

    assertThat(result, equalTo("hey, 1, hey, ho, hum, 2"));
  }

  @Test
  public void shouldHandleFiveInputParameters() throws Exception {
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };
    Func5<String, String, String, String, String, String> node2 = new Func5<String, String, String, String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg1, String arg2, String arg3, String arg4, String arg5) {
        return immediateFuture(arg1 + ", " + arg2 + ", " + arg3 + ", " + arg4 + ", " + arg5 + ", 2");
      }
    };

    Input<String> input = Input.named("in");
    Input<String> input1 = Input.named("innn");
    Input<String> input2 = Input.named("and in");
    Input<String> input3 = Input.named("one more");

    Graph<String> g1 = call(node1).with(input);
    Graph<String> g = call(node2).with(g1, input, input1, input2, input3);

    String result = g
        .bind(input, "hey")
        .bind(input1, "ho")
        .bind(input2, "hum")
        .bind(input3, "häpp")
        .run().get();

    assertThat(result, equalTo("hey, 1, hey, ho, hum, häpp, 2"));
  }

  @Test
  public void shouldPropagateExceptionsToResultFuture() throws Exception {
    final RuntimeException expected = new RuntimeException("expected");
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFailedFuture(expected);
      }
    };
    Func2<String, String, String> node2 = new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg1, String arg2) {
        return immediateFuture(arg1 + ", " + arg2 + ", 2");
      }
    };

    Input<String> input = Input.named("in");

    Graph<String> g1 = call(node1).with(input);
    Graph<String> g = call(node2).with(g1, input);

    thrown.expect(Exception.class);
    thrown.expectCause(withParent(expected));

    g.bind(input, "hey").run().get();
  }

  @Test
  public void shouldPropagateExceptionsToResultFutureForFailedDefault() throws Exception {
    final RuntimeException unexpected = new RuntimeException("not me");
    final RuntimeException expected = new RuntimeException("expected");
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFailedFuture(unexpected);
      }
    };
    Func2<String, String, String> node2 = new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg1, String arg2) {
        return immediateFuture(arg1 + ", " + arg2 + ", 2");
      }
    };

    Input<String> input = Input.named("in");

    Graph<String> g1 = call(node1).with(input).fallback(new AsyncFunction<Throwable, String>() {
      @Override
      public ListenableFuture<String> apply(@Nullable Throwable ignored) throws Exception {
        throw expected;
      }
    });
    Graph<String> g = call(node2).with(g1, input);

    thrown.expect(Exception.class);
    thrown.expectCause(withParent(expected));

    g.bind(input, "hey").run().get();
  }

  @Test
  public void shouldAllowPassingFuturesAsParameters() throws Exception {
    SettableFuture<String> inputFuture = SettableFuture.create();

    Func1<String, Integer> node = new Func1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String arg) {
        return immediateFuture(arg.length());
      }
    };
    Input<String> input = Input.named("input");

    Graph<Integer> g = call(node).with(input);

    inputFuture.set("hello");

    assertThat(g.bind(input, inputFuture).run().get(), equalTo(5));
  }

  @Test
  public void shouldNotBlockOnInputFutures() throws Exception {
    SettableFuture<String> inputFuture = SettableFuture.create();

    Func1<String, Integer> node = new Func1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String arg) {
        return immediateFuture(arg.length());
      }
    };
    Input<String> input = Input.named("input");

    Graph<Integer> g = call(node).with(input);

    ListenableFuture<Integer> future = g.bind(input, inputFuture).run();

    assertThat(future.isDone(), is(false));

    inputFuture.set("hey there");

    assertThat(future.get(), equalTo(9));
  }

  @Test
  public void shouldReportDebugInfoOnFailure() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me").debug(true);

    thrown.expect(Exception.class);
    thrown.expectCause(CoreMatchers.<Throwable>instanceOf(GraphExecutionException.class));
    thrown.expectCause(withParent(expected));

    g.run().get();
  }

  @Test
  public void shouldIncludeCalledNodesInDebugInfo() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me").debug(true);

    verifyCallInfosWhenExecuted(g, debugInfoReport);
  }

  @Test
  public void shouldUseDebugFlagOfInvokedGraph() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> succeedingGraph = setupDebugInfoGraph().debug(false);

    Graph<String> g = call(failingFunction(expected)).with(succeedingGraph).named("failure")
        .bind(debugInfoInput, "fail me").debug(true);

    verifyCallInfosWhenExecuted(g, succeedingGraph);
  }

  @Test
  public void shouldNotIncludeDebugInfoTwice() throws Exception {
    Throwable expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me").debug(true);
    Graph<Integer> g1 = call(new Func1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String arg) {
        return immediateFuture(arg.length());
      }
    }).with(g);

    try {
      g1.run().get();
      fail("expected an exception");
    }
    catch (ExecutionException e) {
      assertThat(e.getCause(), is(instanceOf(GraphExecutionException.class)));
      assertThat(e.getCause().getCause(), equalTo(expected));
    }
  }

  @Test
  public void shouldNotReportDebugInfoIfOff() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me").debug(false);

    thrown.expect(Exception.class);
    thrown.expectCause(not(CoreMatchers.<Throwable>instanceOf(GraphExecutionException.class)));
    thrown.expectCause(withParent(expected));

    g.run().get();
  }

  @Test
  public void shouldSupportTurningOffDebugInfoBeforeBinding() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .debug(false)
        .bind(debugInfoInput, "fail me");

    thrown.expect(Exception.class);
    thrown.expectCause(not(CoreMatchers.<Throwable>instanceOf(GraphExecutionException.class)));
    thrown.expectCause(withParent(expected));

    g.run().get();
  }

  @Test
  public void shouldHaveDebugInfoOnByDefault() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me");

    thrown.expect(Exception.class);
    thrown.expectCause(CoreMatchers.<Throwable>instanceOf(GraphExecutionException.class));
    thrown.expectCause(withParent(expected));

    g.run().get();
  }

  private Graph<String> setupDebugInfoGraph() {
    Func1<String, Integer> func1 = new Func1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String arg) {
        return immediateFuture(arg.length());
      }
    };
    Func2<String, Integer, String> func2 = new Func2<String, Integer, String>() {
      @Override
      public ListenableFuture<String> run(String name, Integer length) {
        return immediateFuture(String.format("Name %s is %d chars long", name, length));
      }
    };

    debugInfoLength = call(func1).with(debugInfoInput).named("length calculation");
    debugInfoReport = call(func2).with(debugInfoInput, debugInfoLength).named("report name and length");

    return debugInfoReport;
  }

  private Func1<String, String> failingFunction(final Throwable expected) {
    return new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFailedFuture(expected);
      }
    };
  }

  private Matcher<? extends Throwable> withParent(final Throwable expected) {
    return new TypeSafeMatcher<Throwable>() {
      @Override
      protected boolean matchesSafely(Throwable item) {
        for (Throwable cause = item ; cause != null ; cause = cause.getCause()) {
          if (cause.equals(expected)) {
            return true;
          }
        }

        return false;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("with parent cause " + expected);
      }
    };
  }

  private void verifyCallInfosWhenExecuted(Graph<String> g, Graph<String> failureNodeInput) throws InterruptedException {
    try {
      g.run().get();
      fail("expected an exception");
    }
    catch (ExecutionException e) {
      GraphExecutionException graphExecutionException = (GraphExecutionException) e.getCause();

      List<CallInfo> calls = graphExecutionException.getCalls();

      for (CallInfo call : calls) {
        System.out
            .println("Called '" + call.getNodeInfo() + "' with: " + call.getParameterValues());
      }

      ParameterValue<String> inputParameterValue = new ParameterValue<String>(
          new BindingDep<String>(debugInfoInput).getNodeInfo(), "fail me");
      ImmutableList<ParameterValue<?>> lengthParams =
          ImmutableList.<ParameterValue<?>>of(inputParameterValue);
      ImmutableList<ParameterValue<?>> reportParams =
          ImmutableList.<ParameterValue<?>>of(inputParameterValue,
                                              new ParameterValue<Integer>(debugInfoLength, 7));
      ImmutableList<ParameterValue<?>> failureParams =
          ImmutableList.<ParameterValue<?>>of(new ParameterValue<String>(failureNodeInput, "Name fail me is 7 chars long"));

      assertThat(filter(calls, new CallInfoMatcher(debugInfoLength, lengthParams)).isEmpty(), is(false));
      assertThat(filter(calls, new CallInfoMatcher(debugInfoReport, reportParams)).isEmpty(), is(false));
      assertThat(filter(calls, new CallInfoMatcher(g, failureParams)).isEmpty(), is(false));
    }
  }

  private class CallInfoMatcher implements Predicate<CallInfo> {

    private final Graph<?> nodeInfo;
    private final ImmutableList<ParameterValue<?>> parameters;

    public CallInfoMatcher(Graph<?> nodeInfo, ImmutableList<ParameterValue<?>> parameters) {
      this.nodeInfo = nodeInfo;
      this.parameters = parameters;
    }

    @Override
    public boolean apply(CallInfo input) {
      return input.getNodeInfo().name().equals(nodeInfo.name()) &&
          input.getParameterValues().equals(parameters);
    }
  }
}
