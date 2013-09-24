package com.spotify.trickle;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.Map;

/**
 * TODO: document!
 */
public class Graph<T> {
  private final Map<Name, Object> inputDependencies;
  private final Map<PNode<?>, ConnectedNode> nodes;
  private final PNode<T> out;

  public Graph(Map<Name, Object> inputDependencies, PNode<T> out, Map<PNode<?>, ConnectedNode> nodeMap) {
    this.out = out;
    this.nodes = nodeMap;
    this.inputDependencies = ImmutableMap.copyOf(inputDependencies);
  }

  public <P> Graph<T> bind(Name input, P value) {
    Map<Name, Object> newInputs = Maps.newHashMap(inputDependencies);
    newInputs.put(input, value);

    return new Graph<>(newInputs, out, nodes);
  }

  public ListenableFuture<T> run() {
    ConnectedNode result = nodes.get(out);

    // this case is safe, because the 'output' node returns type T.
    //noinspection unchecked
    return (ListenableFuture<T>) result.future(inputDependencies, nodes, Maps.<PNode<?>, ListenableFuture<?>>newHashMap());
  }
}
