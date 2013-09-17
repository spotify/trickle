/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import com.spotify.trickle.transform.Transformers;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.spotify.trickle.Name.named;

public final class Trickle {

  private Trickle() {}

  public static class NodeDependencies {
    final List<Dep<?>> inputs = newArrayList();

    public <T> NodeDependencies in(final Node<T> node) {
      checkNotNull(node);
      inputs.add(new NodeDep<>(node, node.returnCls));
      return this;
    }

    public <T> NodeDependencies in(final T value, final Class<T> cls) {
      checkNotNull(cls);
      inputs.add(new ValueDep<>(value, cls));
      return this;
    }

    public <T> NodeDependencies bind(final Name name, final Class<T> cls) {
      checkNotNull(name);
      checkNotNull(cls);
      inputs.add(new BindingDep<>(name, cls));
      return this;
    }

    public <T> NodeDependencies bind(final Dep<T> dep) {
      checkNotNull(dep);
      inputs.add(dep);
      return this;
    }

    public <T> NodeBuilder<T> fallback(final Node<T> node) {
      checkNotNull(node);
      return new NodeBuilder<T>(this, node);
    }

    public <T> NodeBuilder<T> fallback(final T value, final Class<T> cls) {
      checkNotNull(cls);
      return fallback(G().in(value, cls).<T>direct());
    }

    public <T> Node<T> apply(final Class<T> returnCls, final Object obj) {
      return new NodeBuilder<T>(this).apply(returnCls, obj);
    }

    public <T> Node<T> direct() {
      return new NodeBuilder<T>(this).direct();
    }
  }

  public static class NodeBuilder<T> {
    final NodeDependencies nodeDependencies;
    final Optional<Node<T>> fallback;

    public NodeBuilder(NodeDependencies nodeDependencies, Node<T> fallback) {
      this.nodeDependencies = nodeDependencies;
      this.fallback = Optional.fromNullable(fallback);
    }

    public NodeBuilder(NodeDependencies nodeDependencies) {
      this.nodeDependencies = nodeDependencies;
      this.fallback = Optional.absent();
    }

    public Node<T> apply(final Class<T> returnCls, final Object obj) {
      checkNotNull(returnCls);
      final ImmutableList<Dep<?>> deps = ImmutableList.copyOf(nodeDependencies.inputs);


      return new AwaitingInputNode<>(
          deps,
          Transformers.newMethodTransformer(deps, returnCls, obj),
          returnCls,
          fallback
      );
    }

    public Node<T> direct() {
      final ImmutableList<Dep<?>> deps = ImmutableList.copyOf(nodeDependencies.inputs);

      checkArgument(deps.size() == 1, "Direct nodes can only be created from one input");
      final Class<T> returnCls = (Class<T>) deps.get(0).cls;

      return new AwaitingInputNode<>(
          deps,
          Transformers.<T>newDirectTransformer(),
          returnCls,
          fallback
      );
    }
  }

  public static abstract class Dep<T> {
    public final Class<T> cls;

    private Dep(final Class<T> cls) {
      this.cls = cls;
    }
  }

  public static class NodeDep<T> extends Dep<T> {
    public final Node<T> node;

    private NodeDep(final Node<T> node, final Class<T> cls) {
      super(cls);
      this.node = node;
    }
  }

  public static class BindingDep<T> extends Dep<T> {
    public final Name name;

    public BindingDep(Name name, Class<T> cls) {
      super(cls);
      this.name = name;
    }
  }

  public static class ValueDep<T> extends Dep<T> {
    public final T value;

    private ValueDep(final T value, final Class<T> cls) {
      super(cls);
      this.value = value;
    }
  }

  public static NodeDependencies G() {
    return new NodeDependencies();
  }

  public static <T> Node<T> N(
      final ListenableFuture<T> future,
      final Class<T> cls) {

    return new FutureWrapperNode<T>(cls, future);
  }

  public static <T> Dep<T> binding(String name, Class<T> cls) {
    return new BindingDep<T>(named(name), cls);
  }

  public static <T,V> Node<V> chain(
      final Node<T> node,
      final Class<V> cls,
      final Object obj) {

    return Trickle.<V>G().in(node).apply(cls, obj);
  }

  // TODO: optional paths
  // TODO: specifying fallbacks for nodes

}
