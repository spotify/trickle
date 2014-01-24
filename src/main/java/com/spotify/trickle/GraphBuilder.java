package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;

import static com.google.common.base.Optional.of;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Arrays.asList;

/**
 * Builder class that manages most of what's needed to hook up a node into a graph.
 */
class GraphBuilder<R> extends ConfigurableGraph<R> {
  private final String name;
  private final TrickleNode<R> node;

  private final ImmutableList<Dep<?>> inputs;
  private final ImmutableList<Graph<?>> predecessors;

  private final Optional<Function<Throwable, R>> fallback;

  GraphBuilder(String name,
               TrickleNode<R> node,
               ImmutableList<Dep<?>> inputs,
               ImmutableList<Graph<?>> predecessors,
               Optional<Function<Throwable, R>> fallback) {
    this.name = checkNotNull(name, "name");
    this.node = checkNotNull(node, "node");
    this.inputs = checkNotNull(inputs, "inputs");
    this.predecessors = checkNotNull(predecessors, "predecessors");
    this.fallback = checkNotNull(fallback, "fallback");
  }

  GraphBuilder(Node<R> node) {
    this("unnamed", TrickleNode.create(checkNotNull(node, "node")), ImmutableList.<Dep<?>>of(),
         ImmutableList.<Graph<?>>of(), Optional.<Function<Throwable, R>>absent());
  }

  private GraphBuilder<R> withName(String name) {
    return new GraphBuilder<R>(name, node, inputs, predecessors, fallback);
  }

  private GraphBuilder<R> withInputs(ImmutableList<Dep<?>> newInputs) {
    return new GraphBuilder<R>(name, node, with(inputs, newInputs), predecessors, fallback);
  }

  private GraphBuilder<R> withPredecessors(ImmutableList<Graph<?>> newPredecessors) {
    return new GraphBuilder<R>(name, node, inputs, with(predecessors, newPredecessors), fallback);
  }

  private GraphBuilder<R> withFallback(Function<Throwable, R> fallback) {
    return new GraphBuilder<R>(name, node, inputs, predecessors, of(fallback));
  }

  static <E> ImmutableList<E> with(ImmutableList<E> list, List<E> elements) {
    return ImmutableList.<E>builder()
        .addAll(list)
        .addAll(elements)
        .build();
  }

  @SuppressWarnings("unchecked")
  // this method does a couple of unsafe-looking casts, but they are guaranteed by the API to be fine.
  private static ImmutableList<Dep<?>> asDeps(List<Parameter<?>> inputs) {
    ImmutableList.Builder<Dep<?>> result = ImmutableList.builder();

    for (Object input : inputs) {
      if (input instanceof Name) {
        result.add(new BindingDep<Object>((Name<Object>) input));
      } else if (input instanceof Graph) {
        result.add(new GraphDep<Object>((Graph<Object>) input));
      } else {
        throw new IllegalStateException("PROGRAMMER ERROR: illegal input object: " + input);
      }
    }

    return result.build();
  }

  @Override
  public String toString() {
    return name;
  }

  private ConfigurableGraph<R> with(Parameter<?>... inputs) {
    return withInputs(asDeps(asList(inputs)));
  }

  @Override
  public ConfigurableGraph<R> fallback(Function<Throwable, R> handler) {
    return withFallback(handler);
  }

  @Override
  public ConfigurableGraph<R> named(String name) {
    return withName(name);
  }

  @Override
  public ConfigurableGraph<R> after(Graph<?>... predecessors) {
    return withPredecessors(copyOf(predecessors));
  }

  @Override
  public <P> Graph<R> bind(Name<P> name, P value) {
    return new PreparedGraph<R>(this).bind(name, value);
  }

  @Override
  public <P> Graph<R> bind(Name<P> name, ListenableFuture<P> inputFuture) {
    return new PreparedGraph<R>(this).bind(name, inputFuture);
  }

  @Override
  public ListenableFuture<R> run() {
    return new PreparedGraph<R>(this).run();
  }

  @Override
  public ListenableFuture<R> run(Executor executor) {
    return new PreparedGraph<R>(this).run(executor);
  }

  @Override
  ListenableFuture<R> run(TraverseState state) {
    return new PreparedGraph<R>(this).run(state);
  }

  String getName() {
    return name;
  }

  TrickleNode<R> getNode() {
    return node;
  }

  ImmutableList<Dep<?>> getInputs() {
    return inputs;
  }

  ImmutableList<Graph<?>> getPredecessors() {
    return predecessors;
  }

  Optional<Function<Throwable, R>> getFallback() {
    return fallback;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public List<? extends NodeInfo> arguments() {
    return Lists.transform(inputs, new Function<Dep<?>, NodeInfo>() {
      @Override
      public NodeInfo apply(Dep<?> input) {
        return input.getNodeInfo();
      }
    });
  }

  @Override
  public Iterable<? extends NodeInfo> predecessors() {
    return predecessors;
  }

  @Override
  public Type type() {
    return Type.NODE;
  }

  static final class GraphBuilder1<A, R> extends GraphBuilder<R>
      implements Trickle.NeedsParameters1<A, R> {

    GraphBuilder1(Node<R> node) {
      super(node);
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod")
    // this method override is not useless - it ensures that you can only call the 'with'
    // method with a value of the correct type.
    public ConfigurableGraph<R> with(Parameter<A> arg1) {
      return super.with(arg1);
    }
  }

  static final class GraphBuilder2<A, B, R> extends GraphBuilder<R>
      implements Trickle.NeedsParameters2<A, B, R> {

    GraphBuilder2(Node<R> node) {
      super(node);
    }

    @Override
    public ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2) {
      return super.with(arg1, arg2);
    }
  }

  static final class GraphBuilder3<A, B, C, R> extends GraphBuilder<R>
      implements Trickle.NeedsParameters3<A, B, C, R> {

    GraphBuilder3(Node<R> node) {
      super(node);
    }

    @Override
    public ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2, Parameter<C> arg3) {
      return super.with(arg1, arg2, arg3);
    }
  }
}
