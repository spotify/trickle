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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.Trickle.call;
import static com.spotify.trickle.Util.hasAncestor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests of error handling/troubleshooting support.
 */
public class TrickleErrorHandlingTest {
  private static final String INPUT_TO_FAILING_NODE = "report name and length";

  private Input<String> debugInfoInput;
  private Graph<Integer> debugInfoLength;
  private Graph<String> debugInfoReport;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    debugInfoInput = Input.named("weirdName");
  }

  @Test
  public void shouldReportFailingNodeWithDebugOff() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g =
        call(failingFunction(expected)).with(setupDebugInfoGraph()).named("the node that fails")
            .bind(debugInfoInput, "fail me").debug(false);

    thrown.expectMessage("the node that fails");
    thrown.expectMessage(INPUT_TO_FAILING_NODE);

    g.run().get();
  }

  @Test
  public void shouldReportFailingNodeWithDebugOn() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g =
        call(failingFunction(expected)).with(setupDebugInfoGraph()).named("the node that fails")
            .bind(debugInfoInput, "fail me").debug(true);

    thrown.expectMessage("the node that fails");
    thrown.expectMessage(INPUT_TO_FAILING_NODE);

    g.run().get();
  }

  @Test
  public void shouldReportOriginalExceptionOnFailureWithDebugOff() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me").debug(false);

    thrown.expect(hasAncestor(expected));

    g.run().get();
  }

  @Test
  public void shouldReportOriginalExceptionOnFailureWithDebugOn() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me").debug(true);

    thrown.expect(hasAncestor(expected));

    g.run().get();
  }

  @Test
  public void shouldIncludeCalledNodesInDebugInfo() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me").debug(true);

    verifyCallInfos(g, expectedCallInfos(g, debugInfoReport));
  }

  @Test
  public void shouldUseDebugFlagOfInvokedGraph() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> succeedingGraph = setupDebugInfoGraph().debug(false);

    Graph<String> g = call(failingFunction(expected)).with(succeedingGraph).named("failure")
        .bind(debugInfoInput, "fail me").debug(true);

    verifyCallInfos(g, expectedCallInfos(g, succeedingGraph));
  }

  @Test
  public void shouldNotIncludeDebugInfoTwice() throws Exception {
    Throwable expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me").debug(true);
    Graph<Integer> g1 = call(new Func1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(@Nullable String arg) {
        return immediateFuture(arg == null ? 0 : arg.length());
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

    verifyCallInfos(g, ImmutableSet.<ComparableCallInfo>of());
  }

  @Test
  public void shouldSupportTurningOffDebugInfoBeforeBinding() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .debug(false)
        .bind(debugInfoInput, "fail me");

    verifyCallInfos(g, ImmutableSet.<ComparableCallInfo>of());
  }

  @Test
  public void shouldHaveDebugInfoOffByDefault() throws Exception {
    RuntimeException expected = new RuntimeException("expected");
    Graph<String> g = call(failingFunction(expected)).with(setupDebugInfoGraph()).named("failure")
        .bind(debugInfoInput, "fail me");

    verifyCallInfos(g, ImmutableSet.<ComparableCallInfo>of());
  }

  @Test
  public void shouldNotBlockOnUnterminatedInputFuture() throws Exception {
    Input<String> nonTerminating = Input.named("nonTerminating");
    Input<String> failing = Input.named("failing");

    RuntimeException expected = new RuntimeException("expected");

    SettableFuture<String> nonFuture = SettableFuture.create();
    ListenableFuture<String> failFuture = immediateFailedFuture(expected);

    Func2<String, String, String> func = new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(@Nullable String arg1, @Nullable String arg2) {
        return immediateFuture(arg1 + arg2);
      }
    };

    Graph<String> g = call(func).with(nonTerminating, failing);

    thrown.expect(hasAncestor(expected));

    g.bind(failing, failFuture).bind(nonTerminating, nonFuture).run().get();
  }

  private Graph<String> setupDebugInfoGraph() {
    Func1<String, Integer> func1 = new Func1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(@Nullable String arg) {
        return immediateFuture(arg == null ? 0 : arg.length());
      }
    };
    Func2<String, Integer, String> func2 = new Func2<String, Integer, String>() {
      @Override
      public ListenableFuture<String> run(@Nullable String name, @Nullable Integer length) {
        return immediateFuture(String.format("Name %s is %d chars long", name, length));
      }
    };

    debugInfoLength = call(func1).with(debugInfoInput).named("length calculation");
    debugInfoReport = call(func2).with(debugInfoInput, debugInfoLength).named(INPUT_TO_FAILING_NODE);

    return debugInfoReport;
  }

  private Func1<String, String> failingFunction(final Throwable expected) {
    return new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(@Nullable String arg) {
        return immediateFailedFuture(expected);
      }
    };
  }

  private Set<ComparableCallInfo> expectedCallInfos(Graph<String> g,
                                                    Graph<String> failureNodeInput) {
    return ImmutableSet.of(
        new ComparableCallInfo(debugInfoLength.name(),
                               newArrayList(debugInfoInput.getName()),
                               newArrayList("fail me")
        ),

        new ComparableCallInfo(debugInfoReport.name(),
                               newArrayList(debugInfoInput.getName(), debugInfoLength.name()),
                               newArrayList("fail me", "7")
        ),

        new ComparableCallInfo(g.name(),
                               newArrayList(failureNodeInput.name()),
                               newArrayList("Name fail me is 7 chars long")
        )
    );
  }

  private void verifyCallInfos(Graph<String> g, Set<ComparableCallInfo> expectedCallInfos)
      throws InterruptedException {
    try {
      g.run().get();
      fail("expected an exception");
    }
    catch (ExecutionException e) {
      // class cast exceptions here are OK; not getting a GEE at this stage is a bug
      GraphExecutionException graphExecutionException = (GraphExecutionException) e.getCause();

      List<CallInfo> calls = graphExecutionException.getCalls();

      Set<ComparableCallInfo>
          actual =
          Sets.newHashSet(Lists.transform(calls, new Function<CallInfo, ComparableCallInfo>() {
            @Nullable
            @Override
            public ComparableCallInfo apply(@Nullable CallInfo input) {
              List<String> parameterNames = extractParameterNames(input.getParameterValues());
              List<String> parameterValues = extractParameterValues(input.getParameterValues());

              return new ComparableCallInfo(input.getNodeInfo().name(),
                                            parameterNames,
                                            parameterValues);
            }
          }));

      assertThat(actual, equalTo(expectedCallInfos));

    }
  }

  private List<String> extractParameterValues(List<ParameterValue<?>> parameterValues) {
    return Lists.transform(parameterValues,
                           new Function<ParameterValue<?>, String>() {
                             @Nullable
                             @Override
                             public String apply(@Nullable ParameterValue<?> input) {
                               return input.getValue().toString();
                             }
                           });
  }

  private List<String> extractParameterNames(List<ParameterValue<?>> parameterValues) {
    return Lists.transform(parameterValues,
                           new Function<ParameterValue<?>, String>() {
                             @Nullable
                             @Override
                             public String apply(@Nullable ParameterValue<?> input) {
                               return input.getParameter().name();
                             }
                           });
  }



  private static class ComparableCallInfo {
    final String nodeName;
    private final List<String> parameterNames;
    private final List<String> parameterValues;

    private ComparableCallInfo(String nodeName, List<String> parameterNames,
                               List<String> parameterValues) {
      this.nodeName = nodeName;
      this.parameterNames = parameterNames;
      this.parameterValues = parameterValues;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(nodeName, parameterNames, parameterValues);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final ComparableCallInfo other = (ComparableCallInfo) obj;
      return Objects.equal(this.nodeName, other.nodeName) && Objects
          .equal(this.parameterNames, other.parameterNames) && Objects
                 .equal(this.parameterValues, other.parameterValues);
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this)
          .add("nodeName", nodeName)
          .add("parameterNames", parameterNames)
          .add("parameterValues", parameterValues)
          .toString();
    }
  }

}
