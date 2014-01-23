/*
 * Copyright (c) 2014 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

class GraphDep<T> implements Dep<T> {
  private final Graph<T> graph;

  public GraphDep(Graph<T> graph) {
    checkNotNull(graph, "graph");
    this.graph = graph;
  }

  public Graph<T> getGraph() {
    return graph;
  }

  @Override
  public ListenableFuture<T> getFuture(TraverseState state) {
    return state.futureForGraph(graph);
  }

  @Override
  public String name() {
    return graph.name();
  }

  @Override
  public List<? extends GraphElement> inputs() {
    return graph.inputs();
  }

  @Override
  public List<? extends GraphElement> predecessors() {
    return graph.predecessors();
  }

  @Override
  public Type type() {
    return graph.type();
  }
}
