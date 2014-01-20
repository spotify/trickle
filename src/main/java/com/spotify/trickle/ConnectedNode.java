package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.builder;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Represents a node that has been connected to its input dependencies.
 */
class ConnectedNode<N> {
  private final String name;
  private final TrickleNode<N> node;
  private final ImmutableList<Dep<?>> inputs;
  private final ImmutableList<Node<?>> predecessors;
  private final Optional<Function<Throwable, N>> fallback;

  ConnectedNode(String name,
                Node<N> node,
                Iterable<Dep<?>> inputs,
                List<Node<?>> predecessors,
                Optional<Function<Throwable, N>> fallback) {
    this.name = checkNotNull(name, "name");
    this.node = TrickleNode.create(node);
    this.fallback = checkNotNull(fallback, "fallback");
    this.predecessors = ImmutableList.copyOf(predecessors);
    this.inputs = ImmutableList.copyOf(inputs);
  }

  ListenableFuture<N> future(final TraverseState state) {
    final ImmutableList.Builder<ListenableFuture<?>> futuresListBuilder = builder();

    // get node and value dependencies
    for (Dep<?> input : inputs) {
      final ListenableFuture<?> inputFuture = input.getFuture(state);
      futuresListBuilder.add(inputFuture);
    }

    final ImmutableList<ListenableFuture<?>> futures = futuresListBuilder.build();

    // future for signaling propagation - needs to include predecessors, too
    List<ListenableFuture<?>> mustHappenBefore = Lists.newArrayList(futures);
    for (Node<?> predecessor : predecessors) {
      mustHappenBefore.add(state.futureForNode(predecessor));
    }

    final ListenableFuture<List<Object>> allFuture = allAsList(mustHappenBefore);

    checkArgument(inputs.size() == futures.size(), "sanity check result: insane");

    return Futures.withFallback(
        nodeFuture(futures, allFuture, state.getExecutor()), new FutureFallback<N>() {
      @Override
      public ListenableFuture<N> create(Throwable t) {
        if (fallback.isPresent()) {
          return immediateFuture(fallback.get().apply(t));
        }

        return immediateFailedFuture(t);
      }
    });
  }

  private ListenableFuture<N> nodeFuture(final ImmutableList<ListenableFuture<?>> values,
                                         final ListenableFuture<List<Object>> doneSignal,
                                         final Executor executor) {
    return Futures.transform(
        doneSignal,
        new AsyncFunction<List<Object>, N>() {
          @Override
          public ListenableFuture<N> apply(List<Object> input) {
            return node.run(Lists.transform(values, new Function<ListenableFuture<?>, Object>() {
              @Override
              public Object apply(ListenableFuture<?> input) {
                return Futures.getUnchecked(input);
              }
            }));
          }
        },
        executor);
  }

  String getName() {
    return name;
  }

  List<Dep<?>> getInputs() {
    return inputs;
  }

  List<Node<?>> getPredecessors() {
    return predecessors;
  }

  @Override
  public String toString() {
    return name;
  }
}
