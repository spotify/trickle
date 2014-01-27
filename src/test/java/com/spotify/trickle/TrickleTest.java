package com.spotify.trickle;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.Fallbacks.always;
import static com.spotify.trickle.Trickle.call;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration-level Trickle tests.
 */
public class TrickleTest {
  Func0<String> node1;

  SettableFuture<String> future1;
  ListeningExecutorService executorService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

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

    Name<String> inputName = Name.named("theInnnput");
    Graph<String> graph = call(node).with(inputName);

    ListenableFuture<String> future = graph.bind(inputName, "petter").run();
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

    Name<String> inputName = Name.named("theInnnput");
    Graph<String> g1 = call(greet).with(inputName).named("111");
    Graph<String> g2 = call(noop).with(g1).named("222");
    Graph<Integer> g3 = call(node2).with(g2, g1).named("333");

    ListenableFuture<Integer> future = g3.bind(inputName, "rouz").run();
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

    Name<String> inputName = Name.named("theInnnput");
    Graph<String> g11 = call(greet).with(inputName).named("1111");
    Graph<String> g12 = call(greet).with(inputName).named("1112");
    Graph<String> g2 = call(noop).with(g11).named("222");
    Graph<String> g3 = call(node2).with(g2, g11).named("222");
    Graph<String> g4 = call(node2).with(g3, g12).named("333");

    ListenableFuture<String> future = g4.bind(inputName, "rouz").run();
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

    Name<String> input = Name.named("in");

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

    Name<String> input = Name.named("in");
    Name<String> input1 = Name.named("innn");

    Graph<String> g1 = call(node1).with(input);
    Graph<String> g = call(node2).with(g1, input, input1);

    String result = g
        .bind(input, "hey")
        .bind(input1, "ho")
        .run().get();

    assertThat(result, equalTo("hey, 1, hey, ho, 2"));
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

    Name<String> input = Name.named("in");

    Graph<String> g1 = call(node1).with(input);
    Graph<String> g = call(node2).with(g1, input);

    thrown.expect(ExecutionException.class);
    thrown.expectCause(equalTo(expected));

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

    Name<String> input = Name.named("in");

    Graph<String> g1 = call(node1).with(input).fallback(new AsyncFunction<Throwable, String>() {
      @Override
      public ListenableFuture<String> apply(Throwable input) throws Exception {
        throw expected;
      }
    });
    Graph<String> g = call(node2).with(g1, input);

    thrown.expect(ExecutionException.class);
    thrown.expectCause(equalTo(expected));

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
    Name<String> inputName = Name.named("input");

    Graph<Integer> g = call(node).with(inputName);

    inputFuture.set("hello");

    assertThat(g.bind(inputName, inputFuture).run().get(), equalTo(5));
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
    Name<String> inputName = Name.named("input");

    Graph<Integer> g = call(node).with(inputName);

    ListenableFuture<Integer> future = g.bind(inputName, inputFuture).run();

    assertThat(future.isDone(), is(false));

    inputFuture.set("hey there");

    assertThat(future.get(), equalTo(9));
  }
}
