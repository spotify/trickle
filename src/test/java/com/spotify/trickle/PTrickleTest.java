package com.spotify.trickle;

import com.google.common.util.concurrent.*;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration-level PTrickle tests.
 */
public class PTrickleTest {
  PNode<String> node1;

  SettableFuture<String> future1;
  private ListeningExecutorService executorService;

  @Before
  public void setUp() throws Exception {
    future1 = SettableFuture.create();

    node1 = PNode.of(new Object() {
      public ListenableFuture<String> _() {
        return future1;
      }
    });
    executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
  }

  @Test
  public void shouldConstructSingleNodeGraph() throws Exception {
    Graph<String> graph = PTrickle
        .graph(String.class)
        .call(node1)
        .out(node1);

    ListenableFuture<String> actual = graph.run();
    future1.set("hello world!!");

    assertThat(actual.get(), equalTo("hello world!!"));
  }

  @Test
  public void shouldExecuteSingleNodeAsynchronously() throws Exception {
    Graph<String> graph = PTrickle
        .graph(String.class)
        .call(node1)
        .out(node1);

    ListenableFuture<String> actual = graph.run();

    assertThat(actual.isDone(), is(false));

    future1.set("ok, done");
    assertThat(actual.isDone(), is(true));
  }

  @Test
  public void shouldUseInputs() throws Exception {
    PNode<String> node = PNode.of(new Object() {
      public ListenableFuture<String> _(String input) {
        return Futures.immediateFuture("hello " + input + "!");
      }
    });

    Name inputName = Name.named("theInnnput");
    Graph<String> graph = PTrickle
        .graph(String.class)
        .in(inputName)
        .call(node).with(inputName)
        .out(node);

    ListenableFuture<String> future = graph.bind(inputName, "petter").run();
    assertThat(future.get(), equalTo("hello petter!"));
  }

  @Test
  public void shouldMakeAfterHappenAfter() throws Exception {
    final AtomicInteger counter = new AtomicInteger(0);
    final CountDownLatch latch = new CountDownLatch(1);

    PNode<Void> incr1 = PNode.of(new Object() {
      public void _() {
        counter.incrementAndGet();
      }
    });
    PNode<Void> incr2 = PNode.of(new Object() {
      public ListenableFuture<Void> _() throws InterruptedException {
        return executorService.submit(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            latch.await();
            counter.incrementAndGet();
            return null;
          }
        });
      }
    });
    PNode<Integer> result = PNode.of(new Object() {
      public Integer _() {
        return counter.get();
      }
    });

    Graph<Integer> graph = PTrickle
        .graph(Integer.class)
        .call(incr1)
        .call(incr2).after(incr1)
        .call(result).after(incr1, incr2)
        .out(result);

    ListenableFuture<Integer> future = graph.run();

    assertThat(future.isDone(), is(false));
    assertThat(counter.get(), equalTo(1));

    latch.countDown();

    assertThat(future.get(), equalTo(2));
  }
}
