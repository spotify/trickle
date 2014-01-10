package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: document!
 */
abstract class TrickleNode {

  public abstract ListenableFuture<Object> run(List<Object> values);

  static TrickleNode create(Node<?> node) {
    checkNotNull(node);

    if (node instanceof Node0) {
      return new TrickleNode0((Node0<?>) node);
    }
    if (node instanceof Node1) {
      return new TrickleNode1<>((Node1<?, ?>) node);
    }
    if (node instanceof Node2) {
      return new TrickleNode2<>((Node2<?, ?, ?>) node);
    }
    if (node instanceof Node3) {
      return new TrickleNode3<>((Node3<?, ?, ?, ?>) node);
    }

    throw new IllegalArgumentException("unsupported node subclass: " + node.getClass());
  }

  private static class TrickleNode0 extends TrickleNode {
    private final Node0<?> delegate;

    public TrickleNode0(Node0<?> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<Object> run(List<Object> values) {
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return (ListenableFuture<Object>) delegate.run();
    }
  }

  private static class TrickleNode1<A> extends TrickleNode {
    private final Node1<A, ?> delegate;

    public TrickleNode1(Node1<A, ?> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<Object> run(List<Object> values) {
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return (ListenableFuture<Object>) delegate.run((A) values.get(0));
    }
  }

  private static class TrickleNode2<A, B> extends TrickleNode {
    private final Node2<A, B, ?> delegate;

    public TrickleNode2(Node2<A, B, ?> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<Object> run(List<Object> values) {
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return (ListenableFuture<Object>) delegate.run((A) values.get(0), (B) values.get(1));
    }
  }

  private static class TrickleNode3<A, B, C> extends TrickleNode {
    private final Node3<A, B, C, ?> delegate;

    public TrickleNode3(Node3<A, B, C, ?> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<Object> run(List<Object> values) {
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return (ListenableFuture<Object>) delegate.run((A) values.get(0), (B) values.get(1), (C) values.get(2));
    }
  }
}
