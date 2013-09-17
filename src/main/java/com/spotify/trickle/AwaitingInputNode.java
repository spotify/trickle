/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import com.spotify.trickle.transform.Transformer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.builder;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.withFallback;

class AwaitingInputNode<T> extends Node<T> {
  // TODO: move dependencies into base class?
  final ImmutableList<Trickle.Dep<?>> inputs;
  final Transformer<T> transformer;
  final Optional<Node<T>> fallback;

  AwaitingInputNode(
      final ImmutableList<Trickle.Dep<?>> inputs,
      final Transformer<T> transformer,
      final Class<T> returnCls,
      final Optional<Node<T>> fallback) {
    super(returnCls);
    this.inputs = inputs;
    this.transformer = transformer;
    this.fallback = fallback;
  }

  @Override
  protected ListenableFuture<T> future(
      final ImmutableMap<Name, Object> bindings,
      final Map<Node<?>, ListenableFuture<?>> visited) {

    // filter out future and value dependencies
    final ImmutableList.Builder<ListenableFuture<?>> futuresListBuilder = builder();
    final ImmutableList.Builder<Object> valuesListBuilder = builder();

    for (Trickle.Dep<?> input : inputs) {
      // depends on other node
      if (input instanceof Trickle.NodeDep) {
        final Node<?> node = ((Trickle.NodeDep<?>) input).node;

        final ListenableFuture<?> future;
        if (visited.containsKey(node)) {
          future = visited.get(node);
        } else {
          future = node.future(bindings, visited);
          visited.put(node, future);
        }

        futuresListBuilder.add(future);
        valuesListBuilder.add(future);

      // depends on bind
      } else if (input instanceof Trickle.BindingDep) {
        final Trickle.BindingDep<?> bindingDep = (Trickle.BindingDep<?>) input;
        checkArgument(bindings.containsKey(bindingDep.name),
            "Missing bind for name %s, of type %s",
            bindingDep.name, bindingDep.cls);

        final Object bindingValue = bindings.get(bindingDep.name);
        checkArgument(bindingDep.cls.isAssignableFrom(bindingValue.getClass()),
            "Binding type mismatch, expected %s, found %s",
            bindingDep.cls, bindingValue.getClass());

        valuesListBuilder.add(bindingValue);

      // depends on static value
      } else if (input instanceof Trickle.ValueDep) {
        valuesListBuilder.add(((Trickle.ValueDep<?>) input).value);
      }
    }

    final ImmutableList<ListenableFuture<?>> futures = futuresListBuilder.build();
    final ImmutableList<Object> values = valuesListBuilder.build();

    // future for signaling propagation
    final ListenableFuture<List<Object>> allFuture = allAsList(futures);

    checkArgument(inputs.size() == values.size(), "sanity check result: insane");
    final ListenableFuture<T> transformed = transformer.createTransform(values, allFuture);

    return !fallback.isPresent() ? transformed :
        withFallback(transformed, new FutureFallback<T>() {
          @Override
          public ListenableFuture<T> create(Throwable t) throws Exception {
            return fallback.get().future(bindings);
          }
        });
  }

  @Override
  protected void printInfo(PrintStream out, Stack<Boolean> indent) {
    for (int i = 0; i < inputs.size(); i++) {
      final Trickle.Dep<?> input = inputs.get(i);

      indent.push(i != inputs.size() - 1 || fallback.isPresent());
      final String ind = indentStr(indent);

      if (input instanceof Trickle.ValueDep) {
        out.println(ind + "Value(" + ((Trickle.ValueDep<?>)input).value + ")");
      } else if (input instanceof Trickle.BindingDep) {
        final Trickle.BindingDep<?> binding = (Trickle.BindingDep<?>) input;
        out.println(ind + "Binding(" + binding.name + ") (" + binding.cls + ")");
      } else if (input instanceof Trickle.NodeDep) {
        final Node<?> node = ((Trickle.NodeDep<?>) input).node;
        node.printTree(out, indent);
      }

      indent.pop();
    }

    if (fallback.isPresent()) {
//      indent.push(false);
      final String ind = indentStr(indent);
      out.println(ind + "  [fallback]");

      final Node<T> node = fallback.get();
      indent.push(false);
      node.printTree(out, indent);

      indent.pop();
//      indent.pop();
    }
  }
}
