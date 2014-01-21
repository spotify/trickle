/*
 * Copyright (c) 2014 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

class GraphDep<T> implements Dep<T> {
  private final TrickleGraph<T> graph;

  public GraphDep(TrickleGraph<T> graph) {
    checkNotNull(graph, "graph");
    this.graph = graph;
  }

  public TrickleGraph<T> getGraph() {
    return graph;
  }

  @Override
  public ListenableFuture<T> getFuture(TraverseState state) {
    return state.futureForGraph(graph);
  }
}
