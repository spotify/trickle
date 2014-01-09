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

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;


/**
 * TODO: document!
 */
public class Trickle {
  static final Object DEPENDENCY_NOT_INITIALISED = new Object();

  public static <R> GraphBuilder<R> graph(Class<R> returnClass) {
    checkNotNull(returnClass, "returnClass");
    return new GraphBuilder<>();
  }

  public static class GraphBuilder<R> {
    private final Set<Name<?>> inputs;
    private final Set<NodeBuilder<?, R>> nodes;

    private GraphBuilder(Set<Name<?>> inputs, Set<NodeBuilder<?, R>> nodes) {
      this.inputs = ImmutableSet.copyOf(inputs);
      this.nodes = Sets.newHashSet(nodes);
    }

    public GraphBuilder() {
      this(ImmutableSet.<Name<?>>of(), ImmutableSet.<NodeBuilder<?, R>>of());
    }

    public GraphBuilder<R> inputs(Name<?>... dependencies) {
      ImmutableSet.Builder<Name<?>> builder = ImmutableSet.builder();

      builder.addAll(inputs);
      builder.addAll(asList(dependencies));

      return new GraphBuilder<>(builder.build(), nodes);
    }

    public <N> NodeBuilder<N, R> call(Node0<N> node) {
      NodeBuilder<N, R> nodeBuilder = new NodeBuilder<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public <A1, N> NodeBuilder1<A1, N, R> call(Node1<A1, N> node) {
      NodeBuilder1<A1, N, R> nodeBuilder = new NodeBuilder1<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public <A1, A2, N> NodeBuilder2<A1, A2, N, R> call(Node2<A1, A2, N> node) {
      NodeBuilder2<A1, A2, N, R> nodeBuilder = new NodeBuilder2<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public <A1, A2, A3, N> NodeBuilder3<A1, A2, A3, N, R> call(Node3<A1, A2, A3, N> node) {
      NodeBuilder3<A1, A2, A3, N, R> nodeBuilder = new NodeBuilder3<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public TrickleGraph<R> build() {
      Preconditions.checkState(!nodes.isEmpty(), "Empty graph");

      Map<Name<?>, Object> inputDependencies =
          Maps.asMap(inputs, new Function<Name<?>, Object>() {
            @Override
            public Object apply(Name<?> input) {
              return DEPENDENCY_NOT_INITIALISED;
            }
          });

      Node<R> result1 = findSink(nodes);

      return new TrickleGraph<>(inputDependencies, result1, buildNodes(nodes));
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

  public static class NodeBuilder1<A1, N, R> extends NodeBuilder<N, R> {
    private NodeBuilder1(GraphBuilder<R> graphBuilder, Node<N> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 1;
    }

    public NodeBuilder1<A1, N, R> with(Value<A1> arg1) {
      return (NodeBuilder1<A1, N, R>) super.with(arg1);
    }
  }

  public static class NodeBuilder2<A1, A2, N, R> extends NodeBuilder<N, R> {
    private NodeBuilder2(GraphBuilder<R> graphBuilder, Node<N> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 2;
    }

    public NodeBuilder2<A1, A2, N, R> with(Value<A1> arg1, Value<A2> arg2) {
      return (NodeBuilder2<A1, A2, N, R>) super.with(arg1, arg2);
    }
  }

  public static class NodeBuilder3<A1, A2, A3, N, R> extends NodeBuilder<N, R> {
    private NodeBuilder3(GraphBuilder<R> graphBuilder, Node<N> node) {
      super(graphBuilder, node);
    }

    @Override
    int argumentCount() {
      return 3;
    }

    public NodeBuilder3<A1, A2, A3, N, R> with(Value<A1> arg1, Value<A2> arg2, Value<A3> arg3) {
      return (NodeBuilder3<A1, A2, A3, N, R>) super.with(arg1, arg2, arg3);
    }
  }


  public static class NodeBuilder<N, R> {
    final GraphBuilder<R> graphBuilder;
    private final Node<N> node;
    private final List<Value<?>> inputs;
    final List<Node<?>> predecessors;
    private N defaultValue = null;
    private String nodeName = "unnamed";

    private NodeBuilder(GraphBuilder<R> graphBuilder, Node<N> node) {
      this.graphBuilder = graphBuilder;
      this.node = node;
      inputs = new ArrayList<>();
      predecessors = new ArrayList<>();
    }

    private NodeBuilder<N, R> with(Value<?>... inputs) {
      this.inputs.addAll(asList(inputs));
      return this;
    }

    public NodeBuilder<N, R> fallback(N value) {
      defaultValue = value;
      return this;
    }

    public NodeBuilder<N, R> named(String name) {
      nodeName = name;
      return this;
    }

    public <N1> NodeBuilder<N1, R> call(Node0<N1> put1) {
      return graphBuilder.call(put1);
    }

    public <A1, N1> NodeBuilder1<A1, N1, R> call(Node1<A1, N1> put1) {
      return graphBuilder.call(put1);
    }

    public <A1, A2, N1> NodeBuilder2<A1, A2, N1, R> call(Node2<A1, A2, N1> put1) {
      return graphBuilder.call(put1);
    }

    public <A1, A2, A3, N1> NodeBuilder3<A1, A2, A3, N1, R> call(Node3<A1, A2, A3, N1> put1) {
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

    private ConnectedNode connect() {
      if (inputs.size() != argumentCount()) {
        throw new TrickleException(String.format("Incorrect argument count for node '%s' - expected %d, got %d", toString(), argumentCount(), inputs.size()));
      }

      return new ConnectedNode(node, asDeps(inputs), predecessors, Optional.fromNullable(defaultValue));
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
          // TODO: work out this
          result.add(new NodeDep<>((Node<Object>) input, Object.class));
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
}
