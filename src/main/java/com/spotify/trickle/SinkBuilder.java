package com.spotify.trickle;

import com.google.common.base.Function;

import static com.spotify.trickle.Trickle.FinalNeedsParameters1;
import static com.spotify.trickle.Trickle.FinalNeedsParameters2;
import static com.spotify.trickle.Trickle.FinalNeedsParameters3;

/**
 * Implements operations for configuring the sink and building the graph.
 */
class SinkBuilder<R> extends AbstractNodeBuilder<R, R> implements ConfigureOrBuild<R> {

  SinkBuilder(TrickleGraphBuilder<R> graphBuilder, Node<R> node) {
    super(graphBuilder, node);
  }

  protected SinkBuilder<R> with(Value<?>... inputs) {
    addInputs(inputs);
    return this;
  }

  @Override
  public SinkBuilder<R> fallback(Function<Throwable, R> handler) {
    setFallback(handler);
    return this;
  }

  @Override
  public SinkBuilder<R> named(String name) {
    setName(name);
    return this;
  }

  @Override
  public SinkBuilder<R> after(Node<?>... predecessors) {
    addPredecessors(predecessors);
    return this;
  }

  @Override
  public Graph<R> build() {
    return getGraphBuilder().build();
  }


  static final class NodeBuilder1<A, R> extends SinkBuilder<R> implements FinalNeedsParameters1<A, R> {
    NodeBuilder1(TrickleGraphBuilder<R> graphBuilder, Node<R> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 1;
    }

    @Override
    public ConfigureOrBuild<R> with(Value<A> arg1) {
      return super.with(arg1);
    }
  }

  static final class NodeBuilder2<A, B, R> extends SinkBuilder<R> implements FinalNeedsParameters2<A, B, R> {
    NodeBuilder2(TrickleGraphBuilder<R> graphBuilder, Node<R> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 2;
    }

    @Override
    public ConfigureOrBuild<R> with(Value<A> arg1, Value<B> arg2) {
      return super.with(arg1, arg2);
    }
  }

  static final class NodeBuilder3<A, B, C, R> extends SinkBuilder<R> implements FinalNeedsParameters3<A, B, C, R> {
    NodeBuilder3(TrickleGraphBuilder<R> graphBuilder, Node<R> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 3;
    }

    @Override
    public ConfigureOrBuild<R> with(Value<A> arg1, Value<B> arg2, Value<C> arg3) {
      return super.with(arg1, arg2, arg3);
    }
  }
}