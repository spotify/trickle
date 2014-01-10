/*
 * Copyright (c) 2014 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;

class TraverseState {
  final Map<Name<?>, ?> bindings;
  final Map<Node<?>, ConnectedNode<?>> nodes;
  final Map<Node<?>, ListenableFuture<?>> visited;
  final Executor executor;

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
      future = (ListenableFuture<T>) visited.get(node);
    } else {
      // losing type information here, this is fine because the API enforces it
      //noinspection unchecked
      future = (ListenableFuture<T>) nodes.get(node).future(this);
      visited.put(node, future);
    }
    return future;
  }

  public <T> T getBinding(Name<T> name) {
    checkNotNull(name, "name");

    return (T) bindings.get(name);
  }
}
