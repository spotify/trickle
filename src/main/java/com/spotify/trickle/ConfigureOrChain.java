package com.spotify.trickle;

import com.google.common.base.Function;

/**
 * Defines operations available on an intermediate node builder when constructing a graph.
 */
public interface ConfigureOrChain<N, R> extends NodeChainer<R> {
  ConfigureOrChain<N, R> fallback(Function<Throwable, N> handler);

  ConfigureOrChain<N, R> named(String name);

  ConfigureOrChain<N, R> after(Node<?>... predecessors);
}
