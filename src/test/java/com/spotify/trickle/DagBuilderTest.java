package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;
import com.spotify.trickle.graph.DagNode;
import org.junit.Test;

import java.util.Set;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DagBuilderTest {
  Trickle.GraphBuilder<String> graphBuilder;

  @Test
  public void shouldUseCallParameters() throws Exception {
    Node0<Integer> start = new Node0<Integer>() {
      @Override
      public ListenableFuture<Integer> run() {
        return immediateFuture(1);
      }
    };
    Node1<Integer, String> end = new Node1<Integer, String>() {
      @Override
      public ListenableFuture<String> run(Integer arg) {
        return immediateFuture("hi");
      }
    };


    graphBuilder = Trickle.graph(String.class)
        .call(start)
        .call(end).with(start)
        .graphBuilder;

    Set<DagNode<Trickle.NodeBuilder<?,String>>> dagNodes = DagBuilder.buildDag(graphBuilder.nodes);

    assertThat(dagNodes.size(), equalTo(2));
    fail("check more things");
  }

  @Test
  public void shouldSupportAfter() throws Exception {
    fail();
  }

  @Test
  public void shouldSupportDisconnected() throws Exception {
    fail();
  }

  @Test
  public void shouldThrowForCycles() throws Exception {
    fail();
  }
}
