/*
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

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

  private final Optional<AsyncFunction<Throwable, R>> fallback;

  private final boolean debug;

  GraphBuilder(String name,
               TrickleNode<R> node,
               ImmutableList<Dep<?>> inputs,
               ImmutableList<Graph<?>> predecessors,
               Optional<AsyncFunction<Throwable, R>> fallback,
               boolean debug) {
    this.name = checkNotNull(name, "name");
    this.node = checkNotNull(node, "node");
    this.inputs = checkNotNull(inputs, "inputs");
    this.predecessors = checkNotNull(predecessors, "predecessors");
    this.fallback = checkNotNull(fallback, "fallback");
    this.debug = debug;
  }

  GraphBuilder(Func<R> func) {
    this("unnamed", TrickleNode.create(func), ImmutableList.<Dep<?>>of(),
         ImmutableList.<Graph<?>>of(), Optional.<AsyncFunction<Throwable, R>>absent(), false);
  }

  private GraphBuilder<R> withName(String name) {
    return new GraphBuilder<R>(name, node, inputs, predecessors, fallback, debug);
  }

  private GraphBuilder<R> withInputs(ImmutableList<Dep<?>> newInputs) {
    return new GraphBuilder<R>(name, node, with(inputs, newInputs), predecessors, fallback, debug);
  }

  private GraphBuilder<R> withPredecessors(ImmutableList<Graph<?>> newPredecessors) {
    return new GraphBuilder<R>(name, node, inputs, with(predecessors, newPredecessors), fallback,
                               debug);
  }

  private GraphBuilder<R> withFallback(AsyncFunction<Throwable, R> fallback) {
    return new GraphBuilder<R>(name, node, inputs, predecessors, of(fallback), debug);
  }

  private GraphBuilder<R> withDebug(boolean debug) {
    return new GraphBuilder<R>(name, node, inputs, predecessors, fallback, debug);
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
      if (input instanceof Input) {
        result.add(new BindingDep<Object>((Input<Object>) input));
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
  public ConfigurableGraph<R> fallback(AsyncFunction<Throwable, R> handler) {
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
  public Graph<R> debug(boolean debug) {
    return withDebug(debug);
  }

  @Override
  public <P> Graph<R> bind(Input<P> input, P value) {
    return new PreparedGraph<R>(this, debug).bind(input, value);
  }

  @Override
  public <P> Graph<R> bind(Input<P> input, ListenableFuture<P> inputFuture) {
    return new PreparedGraph<R>(this, debug).bind(input, inputFuture);
  }

  @Override
  public ListenableFuture<R> run() {
    return new PreparedGraph<R>(this, debug).run();
  }

  @Override
  public ListenableFuture<R> run(Executor executor) {
    return new PreparedGraph<R>(this, debug).run(executor);
  }

  @Override
  ListenableFuture<R> run(TraverseState state) {
    return new PreparedGraph<R>(this, debug).run(state);
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

  Optional<AsyncFunction<Throwable, R>> getFallback() {
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
      public NodeInfo apply(@Nullable Dep<?> input) {
        // Function.apply() takes a Nullable input, so the compiler warns about the dereference
        // below - in fact, it's only applied to an ImmutableList, which doesn't accept nulls.
        //noinspection ConstantConditions
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

    GraphBuilder1(Func1<A, R> func) {
      super(func);
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

    GraphBuilder2(Func2<A, B, R> func) {
      super(func);
    }

    @Override
    public ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2) {
      return super.with(arg1, arg2);
    }
  }

  static final class GraphBuilder3<A, B, C, R> extends GraphBuilder<R>
      implements Trickle.NeedsParameters3<A, B, C, R> {

    GraphBuilder3(Func3<A, B, C, R> func) {
      super(func);
    }

    @Override
    public ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2, Parameter<C> arg3) {
      return super.with(arg1, arg2, arg3);
    }
  }

  static final class GraphBuilder4<A, B, C, D, R> extends GraphBuilder<R>
      implements Trickle.NeedsParameters4<A, B, C, D, R> {

    GraphBuilder4(Func4<A, B, C, D, R> func) {
      super(func);
    }

    @Override
    public ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2, Parameter<C> arg3, Parameter<D> arg4) {
      return super.with(arg1, arg2, arg3, arg4);
    }
  }

  static final class GraphBuilder5<A, B, C, D, E, R> extends GraphBuilder<R>
      implements Trickle.NeedsParameters5<A, B, C, D, E, R> {

    GraphBuilder5(Func5<A, B, C, D, E, R> func) {
      super(func);
    }

    @Override
    public ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2, Parameter<C> arg3, Parameter<D> arg4, Parameter<E> arg5) {
      return super.with(arg1, arg2, arg3, arg4, arg5);
    }
  }

  static final class ListGraphBuilder<A, R> extends GraphBuilder<R>
      implements Trickle.NeedsParameterList<A, R> {

    ListGraphBuilder(ListFunc<A, R> func) {
      super(func);
    }

    @Override
    public ConfigurableGraph<R> with(List<? extends Parameter<A>> args) {
      return super.with(args.toArray(new Parameter[args.size()]));
    }
  }
}
