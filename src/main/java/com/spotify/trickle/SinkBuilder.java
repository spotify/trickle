package com.spotify.trickle;

import com.google.common.base.Function;

import static com.spotify.trickle.Trickle.FinalNeedsParameters1;
import static com.spotify.trickle.Trickle.FinalNeedsParameters2;
import static com.spotify.trickle.Trickle.FinalNeedsParameters3;
import static java.util.Arrays.asList;

/**
* TODO: document!
*/
class SinkBuilder<R> extends AbstractNodeBuilder<R, R> implements ConfigureOrBuild<R> {

  SinkBuilder(TrickleGraphBuilder<R> graphBuilder, Node<R> node) {
    super(graphBuilder, node);
  }

  protected SinkBuilder<R> with(Value<?>... inputs) {
    this.inputs.addAll(asList(inputs));
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
    this.predecessors.addAll(asList(predecessors));
    return this;
  }

  int argumentCount() {
    return 0;
  }

  @Override
  public String toString() {
    return nodeName;
  }

  @Override
  public Graph<R> build() {
    return graphBuilder.build();
  }


  static final class NodeBuilder1<A, R> extends SinkBuilder<R> implements FinalNeedsParameters1<A, R> {
    NodeBuilder1(TrickleGraphBuilder<R> graphBuilder, Node<R> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 1;
    }

    public NodeBuilder1<A, R> with(Value<A> arg1) {
      return (NodeBuilder1<A, R>) super.with(arg1);
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

    public NodeBuilder2<A, B, R> with(Value<A> arg1, Value<B> arg2) {
      return (NodeBuilder2<A, B, R>) super.with(arg1, arg2);
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

    public NodeBuilder3<A, B, C, R> with(Value<A> arg1, Value<B> arg2, Value<C> arg3) {
      return (NodeBuilder3<A, B, C, R>) super.with(arg1, arg2, arg3);
    }
  }
}
