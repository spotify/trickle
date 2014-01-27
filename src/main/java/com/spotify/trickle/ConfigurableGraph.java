package com.spotify.trickle;

import com.google.common.util.concurrent.AsyncFunction;

/**
 * Defines operations available on an intermediate node builder when constructing a graph.
 */
public abstract class ConfigurableGraph<R> extends Graph<R> {
  public abstract ConfigurableGraph<R> fallback(AsyncFunction<Throwable, R> handler);

  public abstract ConfigurableGraph<R> named(String name);

  public abstract ConfigurableGraph<R> after(Graph<?>... predecessors);

  // prevent construction from outside of package
  ConfigurableGraph() {}
}
