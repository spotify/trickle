package com.spotify.trickle;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Map;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Concrete, non-public implementation of Graph.
 */
class TrickleGraph<T> implements Graph<T> {
  private final Map<Name<?>, Object> inputDependencies;
  private final Map<Node<?>, ConnectedNode> nodes;
  private final Node<T> out;

  TrickleGraph(Map<Name<?>, Object> inputDependencies, Node<T> out, Map<Node<?>, ConnectedNode> nodeMap) {
    this.inputDependencies = ImmutableMap.copyOf(inputDependencies);
    this.out = checkNotNull(out, "out");
    this.nodes = ImmutableMap.copyOf(nodeMap);
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
    Map<Name<?>, Object> newInputs = Maps.newHashMap(inputDependencies);

    checkState(newInputs.put(name, value) == null, "Duplicate binding for name: " + name);

    return new TrickleGraph<>(newInputs, out, nodes);
  }

  @Override
  public ListenableFuture<T> run() {
    return run(MoreExecutors.sameThreadExecutor());
  }

  @Override
  public ListenableFuture<T> run(Executor executor) {
    ConnectedNode result = nodes.get(out);

    // this case is safe, because the 'output' node returns type T.
    //noinspection unchecked
    return (ListenableFuture<T>) result.future(inputDependencies, nodes, Maps.<Node<?>, ListenableFuture<?>>newHashMap(), executor);
  }
}
