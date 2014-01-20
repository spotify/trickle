package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;

/**
 * Builder class that manages most of what's needed to hook up a node into a graph.
 */
abstract class AbstractNodeBuilder<N, R> implements ConnectedNodeBuilder<N> {
  private final TrickleGraphBuilder<R> graphBuilder;
  private final Node<N> node;
  private final List<Value<?>> inputs;
  private final List<Node<?>> predecessors;
  private Function<Throwable, N> fallback = null;
  private String nodeName = "unnamed";

  AbstractNodeBuilder(TrickleGraphBuilder<R> graphBuilder, Node<N> node) {
    this.graphBuilder = checkNotNull(graphBuilder, "graphBuilder");
    this.node = checkNotNull(node, "node");
    inputs = new ArrayList<Value<?>>();
    predecessors = new ArrayList<Node<?>>();
  }

  @Override
  public final ConnectedNode<N> connect() {
    // the argument count should be enforced by the API
    checkState(inputs.size() == argumentCount(), "PROGRAMMER ERROR: Incorrect argument count for node '%s' - expected %d, got %d", toString(), argumentCount(), inputs.size());

    return new ConnectedNode<N>(nodeName, node, asDeps(inputs), predecessors, Optional.fromNullable(fallback));
  }

  int argumentCount() {
    return 0;
  }

  @SuppressWarnings("unchecked")
  // this method does a couple of unsafe-looking casts, but they are guaranteed by the API to be fine.
  private static List<Dep<?>> asDeps(List<Value<?>> inputs) {
    List<Dep<?>> result = Lists.newArrayList();

    for (Object input : inputs) {
      if (input instanceof Name) {
        result.add(new BindingDep<Object>((Name<Object>) input));
      } else if (input instanceof Node) {
        result.add(new NodeDep<Object>((Node<Object>) input));
      } else {
        throw new IllegalStateException("PROGRAMMER ERROR: illegal input object: " + input);
      }
    }

    return result;
  }

  @Override
  public final Node<N> getNode() {
    return node;
  }

  @Override
  public String toString() {
    return nodeName;
  }

  @Override
  public final Iterable<Value<?>> getInputs() {
    return inputs;
  }

  @Override
  public final Iterable<Node<?>> getPredecessors() {
    return predecessors;
  }

  protected void setFallback(Function<Throwable, N> handler) {
    fallback = checkNotNull(handler, "handler");
  }

  protected void setName(String name) {
    nodeName = checkNotNull(name, "name");
  }

  protected void addPredecessors(Node<?>[] predecessors) {
    this.predecessors.addAll(asList(predecessors));
  }

  protected void addInputs(Value<?>[] inputs) {
    this.inputs.addAll(asList(inputs));
  }

  protected TrickleGraphBuilder<R> getGraphBuilder() {
    return graphBuilder;
  }
}
