package com.spotify.trickle;

import com.google.common.testing.AbstractPackageSanityTests;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;

public class PackageSanityTest extends AbstractPackageSanityTests {

  @Before
  @Override
  public void setUp() throws Exception {
    setDefault(Name.class, Name.named("hi", Object.class));
    // this is needed since otherwise, the wrong exception gets thrown by the ConnectedNode
    // constructor - no raw Nodes should ever be used, only NodeN:s, and it seems the
    // AbstractPackageSanityTests creates some non-null instance of Node to use
    setDefault(Node.class, new Node0() {
      @Override
      public ListenableFuture run() {
        throw new UnsupportedOperationException();
      }
    });
    setDefault(TrickleGraphBuilder.class, new TrickleGraphBuilder());
    super.setUp();
  }
}
