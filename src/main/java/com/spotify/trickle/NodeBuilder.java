package com.spotify.trickle;

import com.google.common.base.Function;

/**
 * TODO: document!
 */
public interface NodeBuilder<N, R> {
  ConfigureOrChain<N, R> fallback(Function<Throwable, N> handler);

  ConfigureOrChain<N, R> named(String name);

  ConfigureOrChain<N, R> after(Node<?>... predecessors);
}
