package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
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
import static com.spotify.trickle.Trickle.always;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration-level Trickle tests.
 */
public class TrickleTest {
  Node0<String> node1;

  SettableFuture<String> future1;
  ListeningExecutorService executorService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    future1 = SettableFuture.create();

    node1 = new Node0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return future1;
      }
    };
    executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
  }

  @Test
  public void shouldConstructSingleNodeGraph() throws Exception {
    Graph<String> graph = Trickle
        .graph(String.class)
        .call(node1)
        .build();

    ListenableFuture<String> actual = graph.run(MoreExecutors.sameThreadExecutor());
    future1.set("hello world!!");

    assertThat(actual.get(), equalTo("hello world!!"));
  }

  @Test
  public void shouldExecuteSingleNodeAsynchronously() throws Exception {
    Graph<String> graph = Trickle
        .graph(String.class)
        .call(node1)
        .build();

    ListenableFuture<String> actual = graph.run(MoreExecutors.sameThreadExecutor());

    assertThat(actual.isDone(), is(false));

    future1.set("ok, done");
    assertThat(actual.isDone(), is(true));
  }

  @Test
  public void shouldUseInputs() throws Exception {
    Node1<String, String> node = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String name) {
        return immediateFuture("hello " + name + "!");
      }
    };

    Name<String> inputName = Name.named("theInnnput", String.class);
    Graph<String> graph = Trickle
        .graph(String.class)
        .call(node).with(inputName)
        .build();

    ListenableFuture<String> future = graph.bind(inputName, "petter").run(MoreExecutors.sameThreadExecutor());
    assertThat(future.get(), equalTo("hello petter!"));
  }

  @Test
  public void shouldMakeAfterHappenAfter() throws Exception {
    final AtomicInteger counter = new AtomicInteger(0);
    final CountDownLatch latch = new CountDownLatch(1);

    Node0<Void> incr1 = new Node0<Void>() {
      @Override
      public ListenableFuture<Void> run() {
        counter.incrementAndGet();
        return immediateFuture(null);
      }
    };
    Node0<Void> incr2 = new Node0<Void>() {
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
    Node0<Integer> result = new Node0<Integer>() {
      @Override
      public ListenableFuture<Integer> run() {
        return immediateFuture(counter.get());
      }
    };

    Graph<Integer> graph = Trickle
        .graph(Integer.class)
        .call(incr1)
        .call(incr2).after(incr1)
        .call(result).after(incr1, incr2)
        .build();

    ListenableFuture<Integer> future = graph.run();

    assertThat(future.isDone(), is(false));
    assertThat(counter.get(), equalTo(1));

    latch.countDown();

    assertThat(future.get(), equalTo(2));
  }

  @Test
  public void shouldForwardValues() throws Exception {
    Node0<String> first = new Node0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return immediateFuture("hi there!");
      }
    };
    Node1<String, Integer> second = new Node1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String arg) {
        return immediateFuture(arg.length());
      }
    };

    Graph<Integer> graph = Trickle.graph(Integer.class)
        .call(first)
        .call(second).with(first)
        .build();

    assertThat(graph.run(MoreExecutors.sameThreadExecutor()).get(), equalTo("hi there!".length()));
  }

  @Test
  public void shouldReturnDefaultForFailedCallWithDefault() throws Exception {
    Node0<String> node = new Node0<String>() {
      @Override
      public ListenableFuture<String> run() {
        throw new RuntimeException("expected");
      }
    };

    Graph<String> graph = Trickle.graph(String.class)
        .call(node).fallback(always("fallback response"))
        .build();

    assertThat(graph.run(executorService).get(), equalTo("fallback response"));
  }

  @Test
  public void shouldReturnDefaultForFailedResponseWithDefault() throws Exception {
    Node0<String> node = new Node0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return immediateFailedFuture(new RuntimeException("expected"));
      }
    };

    Graph<String> graph = Trickle.graph(String.class)
        .call(node).fallback(always("fallback response"))
        .build();

    assertThat(graph.run(executorService).get(), equalTo("fallback response"));
  }

  @Test
  public void shouldHandleTwoInputParameters() throws Exception {
    Node1<String, String> node1 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };
    Node2<String, String, String> node2 = new Node2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg1, String arg2) {
        return immediateFuture(arg1 + ", " + arg2 + ", 2");
      }
    };

    Name<String> input = Name.named("in", String.class);

    Graph<String> g = Trickle.graph(String.class)
        .call(node1).with(input)
        .call(node2).with(node1, input)
        .build();

    String result = g.bind(input, "hey").run().get();

    assertThat(result, equalTo("hey, 1, hey, 2"));
  }

  @Test
  public void shouldHandleThreeInputParameters() throws Exception {
    Node1<String, String> node1 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };
    Node3<String, String, String, String> node2 = new Node3<String, String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg1, String arg2, String arg3) {
        return immediateFuture(arg1 + ", " + arg2 + ", " + arg3 + ", 2");
      }
    };

    Name<String> input = Name.named("in", String.class);
    Name<String> input1 = Name.named("innn", String.class);

    Graph<String> g = Trickle.graph(String.class)
        .call(node1).with(input)
        .call(node2).with(node1, input, input1)
        .build();

    String result = g
        .bind(input, "hey")
        .bind(input1, "ho")
        .run().get();

    assertThat(result, equalTo("hey, 1, hey, ho, 2"));
  }

  @Test
  public void shouldPropagateExceptionsToResultFuture() throws Exception {
    final RuntimeException expected = new RuntimeException("expected");
    Node1<String, String> node1 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFailedFuture(expected);
      }
    };
    Node2<String, String, String> node2 = new Node2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg1, String arg2) {
        return immediateFuture(arg1 + ", " + arg2 + ", 2");
      }
    };

    Name<String> input = Name.named("in", String.class);

    Graph<String> g = Trickle.graph(String.class)
        .call(node1).with(input)
        .call(node2).with(node1, input)
        .build();

    thrown.expect(ExecutionException.class);
    thrown.expectCause(equalTo(expected));

    g.bind(input, "hey").run().get();
  }

  @Test
  public void shouldAllowPassingFuturesAsParameters() throws Exception {
    SettableFuture<String> inputFuture = SettableFuture.create();

    Node1<String, Integer> node = new Node1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String arg) {
        return immediateFuture(arg.length());
      }
    };
    Name<String> inputName = Name.named("input", String.class);

    Graph<Integer> g = Trickle.graph(Integer.class)
        .call(node).with(inputName)
        .build();

    inputFuture.set("hello");

    assertThat(g.bind(inputName, inputFuture).run().get(), equalTo(5));
  }

  @Test
  public void shouldNotBlockOnInputFutures() throws Exception {
    SettableFuture<String> inputFuture = SettableFuture.create();

    Node1<String, Integer> node = new Node1<String, Integer>() {
      @Override
      public ListenableFuture<Integer> run(String arg) {
        return immediateFuture(arg.length());
      }
    };
    Name<String> inputName = Name.named("input", String.class);

    Graph<Integer> g = Trickle.graph(Integer.class)
        .call(node).with(inputName)
        .build();


    ListenableFuture<Integer> future = g.bind(inputName, inputFuture).run();

    assertThat(future.isDone(), is(false));

    inputFuture.set("hey there");

    assertThat(future.get(), equalTo(9));
  }

  // TODO: test that verifies blocking behaviour!
}
