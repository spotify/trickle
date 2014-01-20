package com.spotify.trickle;

import com.google.common.base.Function;

/**
 * Defines the operations possible for a node that is not the sink.
 */
public interface ConfigurableChainableNode<N, R> {
  ConfigureOrChain<N, R> fallback(Function<Throwable, N> handler);

  ConfigureOrChain<N, R> named(String name);

  ConfigureOrChain<N, R> after(Node<?>... predecessors);
}
