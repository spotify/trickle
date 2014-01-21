package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;

/**
 * Concrete, non-public implementation of Graph.
 */
class TrickleGraph<T> implements Graph<T> {
  private final Node<T> node;
  private final ConnectedNode<T> connectedNode;
  private final Map<Name<?>, Object> inputDependencies;

  TrickleGraph(Node<T> node, ConnectedNode<T> connectedNode,
               Map<Name<?>, Object> inputDependencies) {
    this.node = checkNotNull(node, "node");
    this.connectedNode = checkNotNull(connectedNode, "connectedNode");
    this.inputDependencies = copyOf(inputDependencies);
  }

  TrickleGraph(Node<T> node, ConnectedNode<T> connectedNode) {
    this(node, connectedNode, Collections.<Name<?>, Object>emptyMap());
  }

  @Override
  public <P> TrickleGraph<T> bind(Name<P> name, P value) {
    return addToInputs(name, value);
  }

  @Override
  public <P> Graph<T> bind(Name<P> name, ListenableFuture<P> inputFuture) {
    return addToInputs(name, inputFuture);
  }

  private TrickleGraph<T> addToInputs(Name<?> name, Object value) {
    Map<Name<?>, Object> newInputs = newHashMap(inputDependencies);

    checkState(newInputs.put(name, value) == null, "Duplicate binding for name: " + name);

    return new TrickleGraph<T>(node, connectedNode, newInputs);
  }

  @Override
  public ListenableFuture<T> run() {
    return run(sameThreadExecutor());
  }

  @Override
  public ListenableFuture<T> run(Executor executor) {
    return run(TraverseState.empty(executor));
  }

  ListenableFuture<T> run(TraverseState state) {
    state.merge(createState(state.getExecutor()));
    return connectedNode.future(state);
  }

  ConnectedNode<T> getConnectedNode() {
    return connectedNode;
  }

  Node<T> getNode() {
    return node;
  }

  private TraverseState createState(Executor executor) {
    return new TraverseState(inputDependencies, executor);
  }
}
