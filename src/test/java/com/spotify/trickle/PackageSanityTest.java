package com.spotify.trickle;

import com.google.common.testing.AbstractPackageSanityTests;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;

import java.util.List;

public class PackageSanityTest extends AbstractPackageSanityTests {

  @Before
  @Override
  public void setUp() throws Exception {
    setDefault(Name.class, Name.named("hi", Object.class));
    // this is needed since otherwise, the wrong exception gets thrown by the ConnectedNode
    // constructor - no raw Nodes should ever be used, only NodeN:s, and it seems the
    // AbstractPackageSanityTests creates some non-null instance of Node to use
    final Node0 node0 = new Node0() {
      @Override
      public ListenableFuture<Object> run() {
        throw new UnsupportedOperationException();
      }
    };
    setDefault(Node.class, node0);
    setDefault(TrickleNode.class, new TrickleNode() {
      @Override
      public ListenableFuture run(List values) {
        throw new UnsupportedOperationException();
      }
    });
    final GraphBuilder graphBuilder = new GraphBuilder(node0);
    setDefault(Graph.class, graphBuilder);
    setDefault(GraphBuilder.class, graphBuilder);

    super.setUp();
  }
}
