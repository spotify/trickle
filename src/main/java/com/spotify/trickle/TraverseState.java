/*
 * Copyright (c) 2014 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;

class TraverseState {
  private final Map<Name<?>, ?> bindings;
  private final Map<Node<?>, ConnectedNode<?>> nodes;
  private final Map<Node<?>, ListenableFuture<?>> visited;
  private final Executor executor;

  TraverseState(Map<Name<?>, ?> bindings,
                Map<Node<?>, ConnectedNode<?>> nodes,
                Map<Node<?>, ListenableFuture<?>> visited,
                Executor executor) {
    this.bindings = checkNotNull(bindings, "bindings");
    this.nodes = checkNotNull(nodes, "nodes");
    this.visited = checkNotNull(visited, "visited");
    this.executor = checkNotNull(executor, "executor");
  }

  <T> ListenableFuture<T> futureForNode(final Node<T> node) {
    final ListenableFuture<T> future;
    if (visited.containsKey(node)) {
      // this cast is fine because the API enforces it
      //noinspection unchecked
      future = (ListenableFuture<T>) visited.get(node);
    } else {
      // losing type information here, this is fine because the API enforces it
      //noinspection unchecked
      future = (ListenableFuture<T>) nodes.get(node).future(this);
      visited.put(node, future);
    }
    return future;
  }

  <T> T getBinding(Name<T> name) {
    checkNotNull(name, "name");

    // this cast is fine because the API enforces it
    //noinspection unchecked
    return (T) bindings.get(name);
  }

  Executor getExecutor() {
    return executor;
  }
}
