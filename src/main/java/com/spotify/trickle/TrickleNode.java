package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper class that simplifies executing nodes with different numbers of parameters.
 */
abstract class TrickleNode<N> {

  public abstract ListenableFuture<N> run(List<Object> values);

  static <V> TrickleNode<V> create(Node<V> node) {
    checkNotNull(node);

    if (node instanceof Node0) {
      return new TrickleNode0<V>((Node0<V>) node);
    }
    if (node instanceof Node1) {
      return new TrickleNode1<Object, V>((Node1<Object, V>) node);
    }
    if (node instanceof Node2) {
      return new TrickleNode2<Object, Object, V>((Node2<Object, Object, V>) node);
    }
    if (node instanceof Node3) {
      return new TrickleNode3<Object, Object, Object, V>((Node3<Object, Object, Object, V>) node);
    }

    throw new IllegalArgumentException("unsupported node subclass: " + node.getClass());
  }

  private static class TrickleNode0<N> extends TrickleNode<N> {
    private final Node0<N> delegate;

    public TrickleNode0(Node0<N> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<N> run(List<Object> values) {
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return delegate.run();
    }
  }

  private static class TrickleNode1<A, N> extends TrickleNode<N> {
    private final Node1<A, N> delegate;

    public TrickleNode1(Node1<A, N> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<N> run(List<Object> values) {
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return delegate.run((A) values.get(0));
    }
  }

  private static class TrickleNode2<A, B, N> extends TrickleNode<N> {
    private final Node2<A, B, N> delegate;

    public TrickleNode2(Node2<A, B, N> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<N> run(List<Object> values) {
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return delegate.run((A) values.get(0), (B) values.get(1));
    }
  }

  private static class TrickleNode3<A, B, C, N> extends TrickleNode<N> {
    private final Node3<A, B, C, N> delegate;

    public TrickleNode3(Node3<A, B, C, N> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<N> run(List<Object> values) {
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return delegate.run((A) values.get(0), (B) values.get(1), (C) values.get(2));
    }
  }
}
