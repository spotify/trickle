/*
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.trickle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Defines how to traverse a {@link Graph}, ensuring that nodes are only invoked once. May
 * optionally collect debug information about each node invocation, simplifying troubleshooting.
 *
 * Implementation note: all state-changing methods - those updating the {@link #visited} and
 * {@link #calls} fields as well as any call to {@link #addBindings(java.util.Map)} - are
 * run from the same thread, when graph execution is started. This class is NOT threadsafe if that
 * should change.
 */
class TraverseState {
  private final Map<Input<?>, Object> bindings;
  private final Executor executor;
  private final boolean collectCallInformation;
  private final Map<Graph<?>, ListenableFuture<?>> visited = newHashMap();
  private final List<FutureCallInformation> calls = newLinkedList();

  TraverseState(Map<Input<?>, Object> bindings, Executor executor, boolean collectCallInformation) {
    this.bindings = checkNotNull(bindings, "bindings");
    this.executor = checkNotNull(executor, "executor");
    this.collectCallInformation = collectCallInformation;
  }

  <T> T getBinding(Input<T> input) {
    checkNotNull(input, "input");

    // this cast is fine because the API enforces it
    //noinspection unchecked
    return (T) bindings.get(input);
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

  public List<FutureCallInformation> getCalls() {
    return ImmutableList.copyOf(calls);
  }

  void addBindings(Map<Input<?>, Object> newBindings) {
    Sets.SetView<Input<?>> intersection = Sets.intersection(bindings.keySet(), newBindings.keySet());
    checkState(intersection.isEmpty(), "Duplicate binding for inputs: %s", intersection);
    bindings.putAll(newBindings);
  }

  FutureCallInformation record(NodeInfo node, List<ListenableFuture<?>> parameterValues) {
    checkNotNull(node, "node");
    checkNotNull(parameterValues, "parameterValues");

    FutureCallInformation futureCallInformation = new FutureCallInformation(node, parameterValues);

    if (!collectCallInformation) {
      return futureCallInformation;
    }

    calls.add(futureCallInformation);

    return futureCallInformation;
  }

  static TraverseState empty(Executor executor, boolean collectCallInformation) {
    return new TraverseState(Maps.<Input<?>, Object>newHashMap(), executor, collectCallInformation);
  }

  static class FutureCallInformation {
    private final NodeInfo node;
    private final List<ListenableFuture<?>> parameterFutures;

    FutureCallInformation(NodeInfo node, List<ListenableFuture<?>> parameterFutures) {
      this.node = checkNotNull(node, "node");
      this.parameterFutures = checkNotNull(parameterFutures, "parameterFutures");
    }

    public boolean isComplete() {
      for (ListenableFuture<?> parameterFuture : parameterFutures) {
        if (!parameterFuture.isDone()) {
          return false;
        }
      }

      return true;
    }

    public NodeInfo getNode() {
      return node;
    }

    public List<ListenableFuture<?>> getParameterFutures() {
      return parameterFutures;
    }
  }
}
