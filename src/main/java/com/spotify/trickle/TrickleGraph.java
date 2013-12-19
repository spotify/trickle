package com.spotify.trickle;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * TODO: document!
 */
class TrickleGraph<T> implements Graph<T> {
  private final Map<Name, Object> inputDependencies;
  private final Map<Node<?>, ConnectedNode> nodes;
  private final Node<T> out;

  TrickleGraph(Map<Name, Object> inputDependencies, Node<T> out, Map<Node<?>, ConnectedNode> nodeMap) {
    this.out = out;
    this.nodes = nodeMap;
    this.inputDependencies = ImmutableMap.copyOf(inputDependencies);
  }

  @Override
  public <P> TrickleGraph<T> bind(Name<P> name, P value) {
    Map<Name, Object> newInputs = Maps.newHashMap(inputDependencies);
    newInputs.put(name, value);

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
