package com.spotify.trickle;

import com.google.common.base.Function;

/**
 * Defines operations available on the sink node builder when constructing a graph.
 */
public interface ConfigureOrBuild<R> extends GraphBuilder<R> {
  ConfigureOrBuild<R> fallback(Function<Throwable, R> handler);

  ConfigureOrBuild<R> named(String name);

  ConfigureOrBuild<R> after(Node<?>... predecessors);

}
