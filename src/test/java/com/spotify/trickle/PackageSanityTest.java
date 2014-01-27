package com.spotify.trickle;

import com.google.common.base.Predicate;
import com.google.common.testing.AbstractPackageSanityTests;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;

import javax.annotation.Nullable;
import java.util.List;

public class PackageSanityTest extends AbstractPackageSanityTests {

  @Before
  @Override
  public void setUp() throws Exception {
    setDefault(Name.class, Name.named("hi", Object.class));
    // this is needed since otherwise, the wrong exception gets thrown by the ConnectedNode
    // constructor - no raw Nodes should ever be used, only NodeN:s, and it seems the
    // AbstractPackageSanityTests creates some non-null instance of Func to use
    final Func0<Object> node0 = new Func0<Object>() {
      @Override
      public ListenableFuture<Object> run() {
        throw new UnsupportedOperationException();
      }
    };
    setDefault(Func.class, node0);
    setDefault(TrickleNode.class, new TrickleNode() {
      @Override
      public ListenableFuture run(List values) {
        throw new UnsupportedOperationException();
      }
    });
    final GraphBuilder<?> graphBuilder = new GraphBuilder<Object>(node0);
    setDefault(Graph.class, graphBuilder);
    setDefault(GraphBuilder.class, graphBuilder);

    ignoreClasses(new Predicate<Class<?>>() {
      @Override
      public boolean apply(@Nullable Class<?> input) {
        return Benchmark.class.equals(input);
      }
    });

    super.setUp();
  }
}
