package com.spotify.trickle;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.builder;
import static com.google.common.util.concurrent.Futures.allAsList;

/**
 * TODO: document!
 */
class ConnectedNode {
  private final Node<?> node;
  private final ImmutableList<Dep<?>> inputs;
  private final ImmutableList<Node<?>> predecessors;
  private final Transformer<?> transformer;

  public ConnectedNode(Node<?> node, Iterable<Dep<?>> inputs, List<Node<?>> predecessors, Transformer<?> transformer) {
    this.node = node;
    this.predecessors = ImmutableList.copyOf(predecessors);
    this.inputs = ImmutableList.copyOf(inputs);
    this.transformer = transformer;
  }

  protected ListenableFuture<?> future(
      final Map<Name, Object> bindings,
      final Map<Node<?>, ConnectedNode> nodes,
      final Map<Node<?>, ListenableFuture<?>> visited) {

    // filter out future and value dependencies
    final ImmutableList.Builder<ListenableFuture<?>> futuresListBuilder = builder();
    final ImmutableList.Builder<Object> valuesListBuilder = builder();

    for (Dep<?> input : inputs) {
      // depends on other node
      if (input instanceof NodeDep) {
        final Node<?> node = ((NodeDep) input).node;

        final ListenableFuture<?> future = futureForNode(bindings, nodes, visited, node);

        futuresListBuilder.add(future);
        valuesListBuilder.add(future);

        // depends on bind
      } else if (input instanceof BindingDep) {
        final BindingDep<?> bindingDep = (BindingDep<?>) input;
        checkArgument(bindings.containsKey(bindingDep.name),
            "Missing bind for name %s, of type %s",
            bindingDep.name, bindingDep.cls);

        final Object bindingValue = bindings.get(bindingDep.name);
        checkArgument(bindingDep.cls.isAssignableFrom(bindingValue.getClass()),
            "Binding type mismatch, expected %s, found %s",
            bindingDep.cls, bindingValue.getClass());

        valuesListBuilder.add(bindingValue);

        // depends on static value
      } else if (input instanceof ValueDep) {
        valuesListBuilder.add(((ValueDep<?>) input).value);
      }
    }

    // add predecessors, too
    for (Node<?> predecessor : predecessors) {
      futuresListBuilder.add(futureForNode(bindings, nodes, visited, predecessor));
    }

    final ImmutableList<ListenableFuture<?>> futures = futuresListBuilder.build();
    final ImmutableList<Object> values = valuesListBuilder.build();

    // future for signaling propagation
    final ListenableFuture<List<Object>> allFuture = allAsList(futures);

    checkArgument(inputs.size() == values.size(), "sanity check result: insane");

    return transformer.createTransform(values, allFuture);
  }

  private ListenableFuture<?> futureForNode(Map<Name, Object> bindings, Map<Node<?>, ConnectedNode> nodes, Map<Node<?>, ListenableFuture<?>> visited, Node<?> node) {
    final ListenableFuture<?> future;
    if (visited.containsKey(node)) {
      future = visited.get(node);
    } else {
      future = nodes.get(node).future(bindings, nodes, visited);
      visited.put(node, future);
    }
    return future;
  }
}
