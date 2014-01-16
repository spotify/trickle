package com.spotify.trickle;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;


/**
 * TODO: document!
 */
public final class Trickle {
  private Trickle() {
    // prevent instantiation
  }

  public static <R> GraphBuilder<R> graph(Class<R> returnClass) {
    checkNotNull(returnClass, "returnClass");
    return new GraphBuilder<>();
  }

  public static <R> GraphBuilder<R> graph(TypeToken<R> returnClass) {
    checkNotNull(returnClass, "returnClass");
    return new GraphBuilder<>();
  }

  public static final class GraphBuilder<R> {
    private final Set<NodeBuilder<?, R>> nodes;

    private GraphBuilder(Set<NodeBuilder<?, R>> nodes) {
      this.nodes = Sets.newHashSet(nodes);
    }

    public GraphBuilder() {
      this(ImmutableSet.<NodeBuilder<?, R>>of());
    }

    public <N> NodeBuilder<N, R> call(Node0<N> node) {
      NodeBuilder<N, R> nodeBuilder = new NodeBuilder<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public <A, N> NeedsParameters1<A, N, R> call(Node1<A, N> node) {
      NodeBuilder1<A, N, R> nodeBuilder = new NodeBuilder1<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public <A, B, N> NeedsParameters2<A, B, N, R> call(Node2<A, B, N> node) {
      NodeBuilder2<A, B, N, R> nodeBuilder = new NodeBuilder2<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public <A, B, C, N> NeedsParameters3<A, B, C, N, R> call(Node3<A, B, C, N> node) {
      NodeBuilder3<A, B, C, N, R> nodeBuilder = new NodeBuilder3<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public TrickleGraph<R> build() {
      Preconditions.checkState(!nodes.isEmpty(), "Empty graph");

      Node<R> result1 = findSink(nodes);

      return new TrickleGraph<>(Collections.<Name<?>, Object>emptyMap(), result1, buildNodes(nodes));
    }

    private Node<R> findSink(Set<NodeBuilder<?, R>> nodes) {
      final Multimap<NodeBuilder<?, R>, NodeBuilder<?, R>> edges = findEdges(nodes);

      Optional<Deque<NodeBuilder<?, R>>> cycle = findOneCycle(edges);

      if (cycle.isPresent()) {
        throw new TrickleException("cycle detected (there may be more): " + Joiner.on(" -> ").join(cycle.get()));
      }

      Set<NodeBuilder<?, R>> sinks = Sets.filter(nodes, new NoNodeDependsOn<>(edges));
      if (sinks.size() != 1) {
        throw new TrickleException("Multiple sinks found: " + sinks);
      }

      NodeBuilder<?, R> sinkBuilder = sinks.iterator().next();

      // note that there is no guarantee that this cast is safe. That's bad, but I'm not sure what
      // to do about it. TODO: think about this
      return (Node<R>) sinkBuilder.node;
    }

    private Optional<Deque<NodeBuilder<?, R>>> findOneCycle(Multimap<NodeBuilder<?, R>, NodeBuilder<?, R>> edges) {

      for (NodeBuilder<?, R> nodeBuilder : edges.keySet()) {
        Optional<Deque<NodeBuilder<?, R>>> cycle = findCycle(nodeBuilder, edges, new LinkedList<NodeBuilder<?, R>>());

        if (cycle.isPresent()) {
          return cycle;
        }
      }

      return Optional.absent();
    }

    private Optional<Deque<NodeBuilder<?, R>>> findCycle(NodeBuilder<?, R> current, Multimap<NodeBuilder<?, R>, NodeBuilder<?, R>> edges, Deque<NodeBuilder<?, R>> visited) {
      if (visited.contains(current)) {
        // add the current node again to close the cycle - this is not perfect, since it can lead
        // to 'cycles' like A -> B -> C -> B, but that's not really a big deal.
        visited.push(current);
        return Optional.of(visited);
      }

      visited.push(current);
      for (NodeBuilder<?, R> nodeBuilder : edges.get(current)) {
        Optional<Deque<NodeBuilder<?, R>>> cycle = findCycle(nodeBuilder, edges, visited);

        if (cycle.isPresent()) {
          return cycle;
        }
      }
      visited.pop();

      return Optional.absent();
    }

    private Multimap<NodeBuilder<?, R>, NodeBuilder<?, R>> findEdges(Set<NodeBuilder<?, R>> nodes) {
      Map<Node<?>, NodeBuilder<?, R>> buildersForNode = extractBuildersPerNode(nodes);

      Multimap<NodeBuilder<?, R>, NodeBuilder<?, R>> result = HashMultimap.create();

      for (NodeBuilder<?, R> node : nodes) {
        for (Value<?> input : node.inputs) {
          if (input instanceof Node) {
            result.put(node, buildersForNode.get(input));
          }
        }

        for (Node<?> predecessor : node.predecessors) {
          result.put(node, buildersForNode.get(predecessor));
        }
      }

      return result;
    }

    private Map<Node<?>, NodeBuilder<?, R>> extractBuildersPerNode(Set<NodeBuilder<?, R>> nodeBuilders) {
      Map<Node<?>, NodeBuilder<?, R>> buildersForNode = Maps.newHashMap();

      for (NodeBuilder<?, R> node : nodeBuilders) {
        buildersForNode.put(node.getNode(), node);
      }
      return buildersForNode;
    }


    private Map<Node<?>, ConnectedNode> buildNodes(Iterable<NodeBuilder<?, R>> nodeBuilders) {
      ImmutableMap.Builder<Node<?>, ConnectedNode> builder = ImmutableMap.builder();

      for (NodeBuilder<?, R> nodeBuilder : nodeBuilders) {
        builder.put(nodeBuilder.node, nodeBuilder.connect());
      }

      return builder.build();
    }

    private static class NoNodeDependsOn<R> implements Predicate<NodeBuilder<?, R>> {
      private final Multimap<NodeBuilder<?, R>, NodeBuilder<?, R>> edges;

      public NoNodeDependsOn(Multimap<NodeBuilder<?, R>, NodeBuilder<?, R>> edges) {
        this.edges = edges;
      }

      @Override
      public boolean apply(NodeBuilder<?, R> input) {
        return !edges.values().contains(input);
      }
    }
  }

  public static final class NodeBuilder1<A, N, R> extends NodeBuilder<N, R> implements NeedsParameters1<A, N, R> {
    private NodeBuilder1(GraphBuilder<R> graphBuilder, Node<N> node) {
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

  public static final class NodeBuilder2<A, B, N, R> extends NodeBuilder<N, R> implements NeedsParameters2<A, B, N, R>  {
    private NodeBuilder2(GraphBuilder<R> graphBuilder, Node<N> node) {
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

  public static final class NodeBuilder3<A, B, C, N, R> extends NodeBuilder<N, R> implements NeedsParameters3<A, B, C, N, R> {
    private NodeBuilder3(GraphBuilder<R> graphBuilder, Node<N> node) {
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


  public static class NodeBuilder<N, R> {
    private final GraphBuilder<R> graphBuilder;
    private final Node<N> node;
    private final List<Value<?>> inputs;
    private final List<Node<?>> predecessors;
    private Function<Throwable, N> fallback = null;
    private String nodeName = "unnamed";

    protected NodeBuilder(GraphBuilder<R> graphBuilder, Node<N> node) {
      this.graphBuilder = graphBuilder;
      this.node = node;
      inputs = new ArrayList<>();
      predecessors = new ArrayList<>();
    }

    private NodeBuilder<N, R> with(Value<?>... inputs) {
      this.inputs.addAll(asList(inputs));
      return this;
    }

    public NodeBuilder<N, R> fallback(Function<Throwable, N> handler) {
      fallback = handler;
      return this;
    }

    public NodeBuilder<N, R> named(String name) {
      nodeName = name;
      return this;
    }

    public <O> NodeBuilder<O, R> call(Node0<O> put1) {
      return graphBuilder.call(put1);
    }

    public <A, O> NeedsParameters1<A, O, R> call(Node1<A, O> put1) {
      return graphBuilder.call(put1);
    }

    public <A, B, O> NeedsParameters2<A, B, O, R> call(Node2<A, B, O> put1) {
      return graphBuilder.call(put1);
    }

    public <A, B, C, O> NeedsParameters3<A, B, C, O, R> call(Node3<A, B, C, O> put1) {
      return graphBuilder.call(put1);
    }

    public NodeBuilder<N, R> after(Node<?>... predecessors) {
      this.predecessors.addAll(asList(predecessors));
      return this;
    }

    @VisibleForTesting
    Node<N> getNode() {
      return node;
    }

    public TrickleGraph<R> build() {
      return graphBuilder.build();
    }

    private ConnectedNode<N> connect() {
      // the argument count should be enforced by the API
      checkState(inputs.size() == argumentCount(), "PROGRAMMER ERROR: Incorrect argument count for node '%s' - expected %d, got %d", toString(), argumentCount(), inputs.size());

      return new ConnectedNode<>(nodeName, node, asDeps(inputs), predecessors, Optional.fromNullable(fallback));
    }

    int argumentCount() {
      return 0;
    }

    private List<Dep<?>> asDeps(List<Value<?>> inputs) {
      List<Dep<?>> result = Lists.newArrayList();

      for (Object input : inputs) {
        if (input instanceof Name) {
          result.add(new BindingDep<>((Name<?>) input, Object.class));
        } else if (input instanceof Node) {
          result.add(new NodeDep<>((Node<?>) input, Object.class));
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
  }

  public static interface NeedsParameters1<A, N, R> {
    NodeBuilder1<A, N, R> with(Value<A> arg1);
  }

  public static interface NeedsParameters2<A, B, N, R> {
    NodeBuilder2<A, B, N, R> with(Value<A> arg1, Value<B> arg2);
  }

  public static interface NeedsParameters3<A, B, C, N, R> {
    NodeBuilder3<A, B, C, N, R> with(Value<A> arg1, Value<B> arg2, Value<C> arg3);
  }

  public static <T> Function<Throwable, T> always(@Nullable final T value) {
    return new Function<Throwable, T>() {
      @Nullable
      @Override
      public T apply(@Nullable Throwable input) {
        return value;
      }
    };
  }
}
