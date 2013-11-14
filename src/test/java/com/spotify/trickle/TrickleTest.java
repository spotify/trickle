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
 * Integration-level Trickle tests.
 */
// there are plenty of methods here that are only called via reflection
@SuppressWarnings("UnusedDeclaration")
public class TrickleTest {
  Node0<String> node1;

  SettableFuture<String> future1;
  private ListeningExecutorService executorService;

  @Before
  public void setUp() throws Exception {
    future1 = SettableFuture.create();

    node1 = () -> future1;
    executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
  }

  @Test
  public void shouldConstructSingleNodeGraph() throws Exception {
    Graph<String> graph = Trickle
        .graph(String.class)
        .call(node1)
        .output(node1);

    ListenableFuture<String> actual = graph.run();
    future1.set("hello world!!");

    assertThat(actual.get(), equalTo("hello world!!"));
  }

  @Test
  public void shouldExecuteSingleNodeAsynchronously() throws Exception {
    Graph<String> graph = Trickle
        .graph(String.class)
        .call(node1)
        .output(node1);

    ListenableFuture<String> actual = graph.run();

    assertThat(actual.isDone(), is(false));

    future1.set("ok, done");
    assertThat(actual.isDone(), is(true));
  }

  @Test
  public void shouldUseInputs() throws Exception {
    Node1<String, String> node = (name -> Futures.immediateFuture("hello " + name + "!"));

    Name inputName = Name.named("theInnnput");
    Graph<String> graph = Trickle
        .graph(String.class)
        .inputs(inputName)
        .call(node).with(inputName)
        .output(node);

    ListenableFuture<String> future = graph.bind(inputName, "petter").run();
    assertThat(future.get(), equalTo("hello petter!"));
  }

  @Test
  public void shouldMakeAfterHappenAfter() throws Exception {
    final AtomicInteger counter = new AtomicInteger(0);
    final CountDownLatch latch = new CountDownLatch(1);

    Node0<Void> incr1 = () -> {
      counter.incrementAndGet();
      return Futures.immediateFuture(null);
    };
    Node0<Void> incr2 = () -> executorService.submit((Callable<Void>) () -> {
      latch.await();
      counter.incrementAndGet();
      return null;
    });
    Node0<Integer> result = () -> Futures.immediateFuture(counter.get());

    Graph<Integer> graph = Trickle
        .graph(Integer.class)
        .call(incr1)
        .call(incr2).after(incr1)
        .call(result).after(incr1, incr2)
        .output(result);

    ListenableFuture<Integer> future = graph.run();

    assertThat(future.isDone(), is(false));
    assertThat(counter.get(), equalTo(1));

    latch.countDown();

    assertThat(future.get(), equalTo(2));
  }
}
