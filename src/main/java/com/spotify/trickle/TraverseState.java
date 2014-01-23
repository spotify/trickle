/*
 * Copyright (c) 2014 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

class TraverseState {
  private final Map<Name<?>, Object> bindings;
  private final Map<Graph<?>, ListenableFuture<?>> visited = newHashMap();
  private final Executor executor;

  TraverseState(Map<Name<?>, Object> bindings, Executor executor) {
    this.bindings = checkNotNull(bindings, "bindings");
    this.executor = checkNotNull(executor, "executor");
  }

  <T> T getBinding(Name<T> name) {
    checkNotNull(name, "name");

    // this cast is fine because the API enforces it
    //noinspection unchecked
    return (T) bindings.get(name);
  }

  <T> ListenableFuture<T> futureForGraph(Graph<T> graph) {
    checkNotNull(graph, "node");
    final ListenableFuture<T> future;

    if (hasVisited(graph)) {
      future = getVisited(graph);
    } else {
      future = graph.run(this);
      visit(graph, future);
    }
    return future;
  }

  <T> boolean hasVisited(Graph<T> graph) {
    checkNotNull(graph, "graph");

    return visited.containsKey(graph);
  }

  <T> ListenableFuture<T> getVisited(Graph<T> graph) {
    checkNotNull(graph, "graph");

    // this cast is fine because the API enforces it
    //noinspection unchecked
    return (ListenableFuture<T>) visited.get(graph);
  }

  <T> void visit(Graph<T> graph, ListenableFuture<T> future) {
    checkNotNull(graph, "graph");
    checkNotNull(future, "future");

    visited.put(graph, future);
  }

  Executor getExecutor() {
    return executor;
  }

  void merge(final TraverseState state) {
    bindings.putAll(state.bindings);
  }

  static TraverseState empty(Executor executor) {
    return new TraverseState(Maps.<Name<?>, Object>newHashMap(), executor);
  }
}
