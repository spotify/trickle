package com.spotify.trickle;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.builder;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * TODO: document!
 */
class ConnectedNode {
  private final Node<?> node;
  private final ImmutableList<Dep<?>> inputs;
  private final ImmutableList<Node<?>> predecessors;
  private final Optional<?> defaultValue;

  public ConnectedNode(Node<?> node, Iterable<Dep<?>> inputs, List<Node<?>> predecessors, Optional<?> defaultValue) {
    this.node = checkNotNull(node, "node");
    this.defaultValue = checkNotNull(defaultValue, "defaultValue");
    this.predecessors = ImmutableList.copyOf(predecessors);
    this.inputs = ImmutableList.copyOf(inputs);
  }

  ListenableFuture<?> future(
      final Map<Name<?>, Object> bindings,
      final Map<Node<?>, ConnectedNode> nodes,
      final Map<Node<?>, ListenableFuture<?>> visited,
      Executor executor) {
    checkNotNull(bindings, "bindings");
    checkNotNull(nodes, "nodes");
    checkNotNull(visited, "visited");
    checkNotNull(executor, "executor");

    // filter out future and value dependencies
    final ImmutableList.Builder<ListenableFuture<?>> futuresListBuilder = builder();
    final ImmutableList.Builder<Object> valuesListBuilder = builder();

    for (Dep<?> input : inputs) {
      // depends on other node
      if (input instanceof NodeDep) {
        final Node<?> node = ((NodeDep) input).node;

        final ListenableFuture<?> future = futureForNode(bindings, nodes, visited, node, executor);

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
      futuresListBuilder.add(futureForNode(bindings, nodes, visited, predecessor, executor));
    }

    final ImmutableList<ListenableFuture<?>> futures = futuresListBuilder.build();
    final ImmutableList<Object> values = valuesListBuilder.build();

    // future for signaling propagation
    final ListenableFuture<List<Object>> allFuture = allAsList(futures);

    checkArgument(inputs.size() == values.size(), "sanity check result: insane");

    return Futures.withFallback(nodeFuture(values, allFuture, executor), new FutureFallback<Object>() {
      @Override
      public ListenableFuture<Object> create(Throwable t) throws Exception {
        if (defaultValue.isPresent()) {
          return immediateFuture(defaultValue.get());
        }

        return immediateFailedFuture(t);
      }
    });
  }

  private ListenableFuture<?> nodeFuture(final ImmutableList<Object> values, ListenableFuture<List<Object>> doneSignal, Executor executor) {
    switch (values.size()) {
      case 0:
        return Futures.transform(doneSignal, new AsyncFunction<Object, Object>() {
          @Override
          public ListenableFuture<Object> apply(Object input) {
            return ((Node0<Object>) node).run();
          }
        }, executor);
      case 1:
        return Futures.transform(doneSignal, new AsyncFunction<Object, Object>() {
          @Override
          public ListenableFuture<Object> apply(Object input) {
            return ((Node1<Object, Object>) node).run(valueAt(values, 0));
          }
        }, executor);
      case 2:
        return Futures.transform(doneSignal, new AsyncFunction<Object, Object>() {
          @Override
          public ListenableFuture<Object> apply(Object input)  {
            return ((Node2<Object, Object, Object>) node).run(valueAt(values, 0), valueAt(values, 1));
          }
        }, executor);
      case 3:
        return Futures.transform(doneSignal, new AsyncFunction<Object, Object>() {
          @Override
          public ListenableFuture<Object> apply(Object input) {
            return ((Node3<Object, Object, Object, Object>) node).run(valueAt(values, 0), valueAt(values, 1), valueAt(values, 2));
          }
        }, executor);
      default:
        throw new UnsupportedOperationException("bleh");
    }
  }

  private Object valueAt(ImmutableList<Object> values, int index) {
    Object value = values.get(index);

    if (value instanceof ListenableFuture) {
      return Futures.getUnchecked((ListenableFuture) value);
    }

    return value;
  }

  private ListenableFuture<?> futureForNode(Map<Name<?>, Object> bindings, Map<Node<?>, ConnectedNode> nodes, Map<Node<?>, ListenableFuture<?>> visited, Node<?> node, Executor executor) {
    final ListenableFuture<?> future;
    if (visited.containsKey(node)) {
      future = visited.get(node);
    } else {
      future = nodes.get(node).future(bindings, nodes, visited, executor);
      visited.put(node, future);
    }
    return future;
  }
}
