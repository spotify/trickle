package com.spotify.trickle;

import com.google.common.base.Function;

/**
 * TODO: document!
 */
public interface ConfigureOrBuild<R> extends GraphBuilder<R> {
  ConfigureOrBuild<R> fallback(Function<Throwable, R> handler);

  ConfigureOrBuild<R> named(String name);

  ConfigureOrBuild<R> after(Node<?>... predecessors);

}
