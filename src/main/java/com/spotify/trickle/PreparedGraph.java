/*
 * Copyright (c) 2014 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.builder;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;

/**
 * A decorator class for Graph that holds bound values for input names thus making
 * graph runnable.
 *
 * This class is immutable and thread safe. Calls to any of the bind methods will
 * return a new instance containing that binding.
 *
 * @param <R>  The return type of the graph
 */
final class PreparedGraph<R> extends Graph<R> {

  private final GraphBuilder<R> graph;
  private final ImmutableMap<Name<?>, Object> inputBindings;

  private PreparedGraph(GraphBuilder<R> graph, ImmutableMap<Name<?>, Object> inputBindings) {
    this.graph = checkNotNull(graph, "graph");
    this.inputBindings = checkNotNull(inputBindings, "inputBindings");
  }

  PreparedGraph(GraphBuilder<R> graph) {
    this(graph, ImmutableMap.<Name<?>, Object>of());
  }

  @Override
  public <P> Graph<R> bind(Name<P> name, P value) {
    return addToInputs(name, value);
  }

  @Override
  public <P> Graph<R> bind(Name<P> name, ListenableFuture<P> inputFuture) {
    return addToInputs(name, inputFuture);
  }

  @Override
  public ListenableFuture<R> run() {
    return run(sameThreadExecutor());
  }

  @Override
  public ListenableFuture<R> run(Executor executor) {
    return run(TraverseState.empty(executor));
  }

  @Override
  ListenableFuture<R> run(TraverseState state) {
    state.addBindings(inputBindings);
    return future(state);
  }

  private ListenableFuture<R> future(final TraverseState state) {
    final ImmutableList.Builder<ListenableFuture<?>> futuresListBuilder = builder();

    // get node and value dependencies
    for (Dep<?> input : graph.getInputs()) {
      final ListenableFuture<?> inputFuture = input.getFuture(state);
      futuresListBuilder.add(inputFuture);
    }

    final ImmutableList<ListenableFuture<?>> futures = futuresListBuilder.build();

    // future for signaling propagation - needs to include predecessors, too
    List<ListenableFuture<?>> mustHappenBefore = Lists.newArrayList(futures);
    for (Graph<?> predecessor : graph.getPredecessors()) {
      mustHappenBefore.add(state.futureForGraph(predecessor));
    }

    final ListenableFuture<List<Object>> allFuture = allAsList(mustHappenBefore);

    checkArgument(graph.getInputs().size() == futures.size(), "sanity check result: insane");

    return Futures.withFallback(
        nodeFuture(futures, allFuture, state.getExecutor()), new FutureFallback<R>() {
      @Override
      public ListenableFuture<R> create(Throwable t) {
        if (graph.getFallback().isPresent()) {
          return immediateFuture(graph.getFallback().get().apply(t));
        }

        return immediateFailedFuture(t);
      }
    });
  }

  private ListenableFuture<R> nodeFuture(final ImmutableList<ListenableFuture<?>> values,
                                         final ListenableFuture<List<Object>> doneSignal,
                                         final Executor executor) {
    return Futures.transform(
        doneSignal,
        new AsyncFunction<List<Object>, R>() {
          @Override
          public ListenableFuture<R> apply(List<Object> input) {
            return graph.getNode().run(Lists.transform(values, new Function<ListenableFuture<?>, Object>() {
              @Override
              public Object apply(ListenableFuture<?> input) {
                return Futures.getUnchecked(input);
              }
            }));
          }
        },
        executor);
  }

  private TraverseState createState(Executor executor) {
    return new TraverseState(inputBindings, executor);
  }

  private PreparedGraph<R> addToInputs(Name<?> name, Object value) {
    checkState(!inputBindings.containsKey(name), "Duplicate binding for name: " + name);

    return new PreparedGraph<R>(
        graph,
        ImmutableMap.<Name<?>, Object>builder()
          .putAll(inputBindings)
          .put(name, value)
          .build());
  }

  @Override
  public String name() {
    return graph.name();
  }

  @Override
  public List<? extends GraphElement> inputs() {
    return graph.inputs();
  }

  @Override
  public List<? extends GraphElement> predecessors() {
    return graph.predecessors();
  }

  @Override
  public Type type() {
    return graph.type();
  }
}
