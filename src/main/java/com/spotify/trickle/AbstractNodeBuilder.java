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
abstract class AbstractNodeBuilder<R> implements ConnectedNodeBuilder<R> {
  private String name = "unnamed";
  private final Node<R> node;
  private final List<Value<?>> inputs;
  private final List<Graph<?>> predecessors;
  private Function<Throwable, R> fallback = null;

//  private final ConnectedNodeBuilder<R> node;

  AbstractNodeBuilder(Node<R> node) {
    this.node = checkNotNull(node, "node");
    inputs = new ArrayList<Value<?>>();
    predecessors = new ArrayList<Graph<?>>();
  }

  @Override
  public final ConnectedNode<R> connect() {
    // the argument count should be enforced by the API
    checkState(inputs.size() == argumentCount(),
               "PROGRAMMER ERROR: Incorrect argument count for node '%s' - expected %d, got %d",
               toString(), argumentCount(), inputs.size());

    return new ConnectedNode<R>(name, node, asDeps(inputs), asGraphs(predecessors),
                                Optional.fromNullable(fallback));
  }

  TrickleGraph<R> getGraph() {
    return new TrickleGraph<R>(node, connect());
  }

  int argumentCount() {
    return 0;
  }

  protected void setFallback(Function<Throwable, R> handler) {
    fallback = checkNotNull(handler, "handler");
  }

  protected void setName(String name) {
    this.name = checkNotNull(name, "name");
  }

  protected void addPredecessors(Graph<?>[] predecessors) {
    this.predecessors.addAll(asList(predecessors));
  }

  protected void addInputs(Value<?>[] inputs) {
    this.inputs.addAll(asList(inputs));
  }

  private static List<TrickleGraph<?>> asGraphs(List<Graph<?>> predecessors) {
    List<TrickleGraph<?>> result = Lists.newArrayList();

    for (Graph<?> predecessor : predecessors) {
      if (predecessor instanceof AbstractNodeBuilder) {
        AbstractNodeBuilder<?> nodeBuilder = (AbstractNodeBuilder<?>) predecessor;
        result.add(nodeBuilder.getGraph());
      }
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  // this method does a couple of unsafe-looking casts, but they are guaranteed by the API to be fine.
  private static List<Dep<?>> asDeps(List<Value<?>> inputs) {
    List<Dep<?>> result = Lists.newArrayList();

    for (Object input : inputs) {
      if (input instanceof Name) {
        result.add(new BindingDep<Object>((Name<Object>) input));
      } else if (input instanceof AbstractNodeBuilder) {
        result.add(new GraphDep<Object>(((AbstractNodeBuilder<Object>) input).getGraph()));
      } else {
        throw new IllegalStateException("PROGRAMMER ERROR: illegal input object: " + input);
      }
    }

    return result;
  }

  @Override
  public String toString() {
    return name;
  }
}
