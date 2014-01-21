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
  private final Map<Node<?>, ListenableFuture<?>> visited = newHashMap();
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

  <T> ListenableFuture<T> futureForGraph(TrickleGraph<T> graph) {
    checkNotNull(graph, "node");
    final Node<T> node = graph.getNode();
    final ListenableFuture<T> future;

    if (hasVisited(node)) {
      future = getVisited(node);
    } else {
      future = graph.run(this);
      visit(node, future);
    }
    return future;
  }

  <T> boolean hasVisited(Node<T> node) {
    checkNotNull(node, "node");

    return visited.containsKey(node);
  }

  <T> ListenableFuture<T> getVisited(Node<T> node) {
    checkNotNull(node, "node");

    // this cast is fine because the API enforces it
    //noinspection unchecked
    return (ListenableFuture<T>) visited.get(node);
  }

  <T> void visit(Node<T> node, ListenableFuture<T> future) {
    checkNotNull(node, "node");
    checkNotNull(future, "future");

    visited.put(node, future);
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
