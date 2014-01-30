/*
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

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
    if (func instanceof Func4) {
      return new TrickleNode4<Object, Object, Object, Object, V>((Func4<Object, Object, Object, Object, V>) func);
    }
    if (func instanceof Func5) {
      return new TrickleNode5<Object, Object, Object, Object, Object, V>((Func5<Object, Object, Object, Object, Object, V>) func);
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

  private static class TrickleNode4<A, B, C, D, N> extends TrickleNode<N> {
    private final Func4<A, B, C, D, N> delegate;

    public TrickleNode4(Func4<A, B, C, D, N> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<N> run(List<Object> values) {
      //CHECKSTYLE:OFF - we don't care that these are magic numbers
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return delegate.run((A) values.get(0), (B) values.get(1), (C) values.get(2), (D) values.get(3));
      //CHECKSTYLE:ON
    }
  }

  private static class TrickleNode5<A, B, C, D, E, N> extends TrickleNode<N> {
    private final Func5<A, B, C, D, E, N> delegate;

    public TrickleNode5(Func5<A, B, C, D, E, N> node) {
      delegate = node;
    }

    @Override
    public ListenableFuture<N> run(List<Object> values) {
      //CHECKSTYLE:OFF - we don't care that these are magic numbers
      // this cast is safe, as guaranteed by the API for creating nodes
      //noinspection unchecked
      return delegate.run((A) values.get(0), (B) values.get(1), (C) values.get(2), (D) values.get(3), (E) values.get(4));
      //CHECKSTYLE:ON
    }
  }
}
