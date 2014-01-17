package com.spotify.trickle;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.spotify.trickle.Trickle.NeedsParameters1;
import static com.spotify.trickle.Trickle.NeedsParameters2;
import static java.util.Arrays.asList;

/**
* TODO: document!
*/
class ChainingNodeBuilder<N, R> extends AbstractNodeBuilder<N, R> implements ConfigureOrChain<N, R> {

  ChainingNodeBuilder(TrickleGraphBuilder<R> graphBuilder, Node<N> node) {
    super(graphBuilder, node);
  }

  protected ChainingNodeBuilder<N, R> with(Value<?>... inputs) {
    this.inputs.addAll(asList(inputs));
    return this;
  }

  @Override
  public ChainingNodeBuilder<N, R> fallback(Function<Throwable, N> handler) {
    setFallback(handler);
    return this;
  }

  @Override
  public ChainingNodeBuilder<N, R> named(String name) {
    setName(name);
    return this;
  }

  @Override
  public <O> ChainingNodeBuilder<O, R> call(Node0<O> put1) {
    return graphBuilder.call(put1);
  }

  @Override
  public <A, O> NeedsParameters1<A, O, R> call(Node1<A, O> put1) {
    return graphBuilder.call(put1);
  }

  @Override
  public <A, B, O> NeedsParameters2<A, B, O, R> call(Node2<A, B, O> put1) {
    return graphBuilder.call(put1);
  }

  @Override
  public <A, B, C, O> Trickle.NeedsParameters3<A, B, C, O, R> call(Node3<A, B, C, O> put1) {
    return graphBuilder.call(put1);
  }

  @Override
  public ConfigureOrBuild<R> finallyCall(Node0<R> node) {
    return graphBuilder.finallyCall(node);
  }

  @Override
  public <A> Trickle.FinalNeedsParameters1<A, R> finallyCall(Node1<A, R> node) {
    return graphBuilder.finallyCall(node);
  }

  @Override
  public <A, B> Trickle.FinalNeedsParameters2<A, B, R> finallyCall(Node2<A, B, R> node) {
    return graphBuilder.finallyCall(node);
  }

  @Override
  public <A, B, C> Trickle.FinalNeedsParameters3<A, B, C, R> finallyCall(Node3<A, B, C, R> node) {
    return graphBuilder.finallyCall(node);
  }

  @Override
  public ChainingNodeBuilder<N, R> after(Node<?>... predecessors) {
    this.predecessors.addAll(asList(predecessors));
    return this;
  }


  static final class NodeBuilder1<A, N, R> extends ChainingNodeBuilder<N, R> implements NeedsParameters1<A, N, R> {
     NodeBuilder1(TrickleGraphBuilder<R> graphBuilder, Node<N> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 1;
    }

    public NodeBuilder1<A, N, R> with(Value<A> arg1) {
      return (NodeBuilder1<A, N, R>) super.with(arg1);
    }
  }

  static final class NodeBuilder2<A, B, N, R> extends ChainingNodeBuilder<N, R> implements NeedsParameters2<A, B, N, R> {
    NodeBuilder2(TrickleGraphBuilder<R> graphBuilder, Node<N> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 2;
    }

    public NodeBuilder2<A, B, N, R> with(Value<A> arg1, Value<B> arg2) {
      return (NodeBuilder2<A, B, N, R>) super.with(arg1, arg2);
    }
  }

  static final class NodeBuilder3<A, B, C, N, R> extends ChainingNodeBuilder<N, R> implements Trickle.NeedsParameters3<A, B, C, N, R> {
    NodeBuilder3(TrickleGraphBuilder<R> graphBuilder, Node<N> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 3;
    }

    public NodeBuilder3<A, B, C, N, R> with(Value<A> arg1, Value<B> arg2, Value<C> arg3) {
      return (NodeBuilder3<A, B, C, N, R>) super.with(arg1, arg2, arg3);
    }
  }
}
