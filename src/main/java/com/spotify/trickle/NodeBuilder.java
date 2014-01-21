package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

import static com.spotify.trickle.Trickle.NeedsParameters1;
import static com.spotify.trickle.Trickle.NeedsParameters2;

/**
* Implements support for configuring a node or adding another node to the graph under construction.
*/
class NodeBuilder<R> extends AbstractNodeBuilder<R> implements ConfigureNode<R> {

  NodeBuilder(Node<R> node) {
    super(node);
  }

  protected NodeBuilder<R> with(Value<?>... inputs) {
    addInputs(inputs);
    return this;
  }

  @Override
  public NodeBuilder<R> fallback(Function<Throwable, R> handler) {
    setFallback(handler);
    return this;
  }

  @Override
  public NodeBuilder<R> named(String name) {
    setName(name);
    return this;
  }

  @Override
  public NodeBuilder<R> after(Graph<?>... predecessors) {
    addPredecessors(predecessors);
    return this;
  }

  @Override
  public <P> Graph<R> bind(Name<P> name, P value) {
    return getGraph().bind(name, value);
  }

  @Override
  public <P> Graph<R> bind(Name<P> name, ListenableFuture<P> inputFuture) {
    return getGraph().bind(name, inputFuture);
  }

  @Override
  public ListenableFuture<R> run() {
    return getGraph().run();
  }

  @Override
  public ListenableFuture<R> run(Executor executor) {
    return getGraph().run(executor);
  }

  static final class NodeBuilder1<A, R> extends NodeBuilder<R> implements NeedsParameters1<A, R> {
     NodeBuilder1(Node<R> node) {
      super(node);
    }

    @Override
    int argumentCount() {
      return 1;
    }

    @Override
    public ConfigureNode<R> with(Value<A> arg1) {
      return super.with(arg1);
    }
  }

  static final class NodeBuilder2<A, B, R> extends NodeBuilder<R> implements NeedsParameters2<A, B, R> {
    NodeBuilder2(Node<R> node) {
      super(node);
    }

    @Override
    int argumentCount() {
      return 2;
    }

    @Override
    public ConfigureNode<R> with(Value<A> arg1, Value<B> arg2) {
      return super.with(arg1, arg2);
    }
  }

  static final class NodeBuilder3<A, B, C, R> extends NodeBuilder<R> implements Trickle.NeedsParameters3<A, B, C, R> {
    NodeBuilder3(Node<R> node) {
      super(node);
    }

    @Override
    int argumentCount() {
      return 3;
    }

    @Override
    public ConfigureNode<R> with(Value<A> arg1, Value<B> arg2, Value<C> arg3) {
      return super.with(arg1, arg2, arg3);
    }
  }
}
