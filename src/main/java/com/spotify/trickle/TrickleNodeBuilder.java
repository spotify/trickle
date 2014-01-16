package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

import static com.spotify.trickle.Trickle.NeedsParameters1;
import static com.spotify.trickle.Trickle.NeedsParameters2;
import static java.util.Arrays.asList;

/**
* TODO: document!
*/
class TrickleNodeBuilder<N, R> implements NodeBuilder<N,R>, ConfigureOrChain<N, R>, GraphBuilder<R> {
  private final TrickleGraphBuilder<R> graphBuilder;
  private final Node<N> node;
  private final List<Value<?>> inputs;
  private final List<Node<?>> predecessors;
  private Function<Throwable, N> fallback = null;
  private String nodeName = "unnamed";

  TrickleNodeBuilder(TrickleGraphBuilder<R> graphBuilder, Node<N> node) {
    this.graphBuilder = graphBuilder;
    this.node = node;
    inputs = new ArrayList<>();
    predecessors = new ArrayList<>();
  }

  protected TrickleNodeBuilder<N, R> with(Value<?>... inputs) {
    this.inputs.addAll(asList(inputs));
    return this;
  }

  @Override
  public TrickleNodeBuilder<N, R> fallback(Function<Throwable, N> handler) {
    fallback = handler;
    return this;
  }

  @Override
  public TrickleNodeBuilder<N, R> named(String name) {
    nodeName = name;
    return this;
  }

  @Override
  public <O> TrickleNodeBuilder<O, R> call(Node0<O> put1) {
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
    throw new UnsupportedOperationException();
  }

  @Override
  public <A> Trickle.FinalNeedsParameters1<A, R> finallyCall(Node1<A, R> node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <A, B> Trickle.FinalNeedsParameters2<A, B, R> finallyCall(Node2<A, B, R> node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <A, B, C> Trickle.FinalNeedsParameters3<A, B, C, R> finallyCall(Node3<A, B, C, R> node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TrickleNodeBuilder<N, R> after(Node<?>... predecessors) {
    this.predecessors.addAll(asList(predecessors));
    return this;
  }

  Node<N> getNode() {
    return node;
  }

  @Override
  public Graph<R> build() {
    return graphBuilder.build();
  }

  ConnectedNode<N> connect() {
    // the argument count should be enforced by the API
    checkState(inputs.size() == argumentCount(), "PROGRAMMER ERROR: Incorrect argument count for node '%s' - expected %d, got %d", toString(), argumentCount(), inputs.size());

    return new ConnectedNode<>(nodeName, node, asDeps(inputs), predecessors, Optional.fromNullable(fallback));
  }

  int argumentCount() {
    return 0;
  }

  private static List<Dep<?>> asDeps(List<Value<?>> inputs) {
    List<Dep<?>> result = Lists.newArrayList();

    for (Object input : inputs) {
      if (input instanceof Name) {
        result.add(new BindingDep<>((Name<?>) input));
      } else if (input instanceof Node) {
        result.add(new NodeDep<>((Node<?>) input));
      } else {
        throw new IllegalStateException("PROGRAMMER ERROR: illegal input object: " + input);
      }
    }

    return result;
  }

  @Override
  public String toString() {
    return nodeName;
  }

  List<Value<?>> getInputs() {
    return inputs;
  }

  List<Node<?>> getPredecessors() {
    return predecessors;
  }

  static final class NodeBuilder1<A, N, R> extends TrickleNodeBuilder<N, R> implements NeedsParameters1<A, N, R> {
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

  static final class NodeBuilder2<A, B, N, R> extends TrickleNodeBuilder<N, R> implements NeedsParameters2<A, B, N, R> {
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

  static final class NodeBuilder3<A, B, C, N, R> extends TrickleNodeBuilder<N, R> implements Trickle.NeedsParameters3<A, B, C, N, R> {
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
