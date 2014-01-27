package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper class that simplifies executing nodes with different numbers of parameters.
 */
abstract class TrickleNode<N> {

  public abstract ListenableFuture<N> run(List<Object> values);

  static <V> TrickleNode<V> create(Func<V> func) {
    checkNotNull(func);

    if (func instanceof Func0) {
      return new TrickleNode0<V>((Func0<V>) func);
    }
    if (func instanceof Func1) {
      return new TrickleNode1<Object, V>((Func1<Object, V>) func);
    }
    if (func instanceof Func2) {
      return new TrickleNode2<Object, Object, V>((Func2<Object, Object, V>) func);
    }
    if (func instanceof Func3) {
      return new TrickleNode3<Object, Object, Object, V>((Func3<Object, Object, Object, V>) func);
    }

    throw new IllegalArgumentException("unsupported func subclass: " + func.getClass());
  }

  private static class TrickleNode0<N> extends TrickleNode<N> {
    private final Func0<N> delegate;

    public TrickleNode0(Func0<N> node) {
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
    private final Func1<A, N> delegate;

    public TrickleNode1(Func1<A, N> node) {
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
    private final Func2<A, B, N> delegate;

    public TrickleNode2(Func2<A, B, N> node) {
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
    private final Func3<A, B, C, N> delegate;

    public TrickleNode3(Func3<A, B, C, N> node) {
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
