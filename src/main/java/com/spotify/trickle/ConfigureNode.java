package com.spotify.trickle;

import com.google.common.base.Function;

/**
 * Defines operations available on an intermediate node builder when constructing a graph.
 */
public interface ConfigureNode<R> extends Graph<R> {
  ConfigureNode<R> fallback(Function<Throwable, R> handler);

  ConfigureNode<R> named(String name);

  ConfigureNode<R> after(Graph<?>... predecessors);
}
