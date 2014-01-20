package com.spotify.trickle;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
* Top-level builder class.
*/
final class TrickleGraphBuilder<R> implements GraphBuilder<R>, NodeChainer<R> {
  private final Set<ConnectedNodeBuilder<?>> nodes;

  TrickleGraphBuilder(Set<ConnectedNodeBuilder<?>> nodes) {
    this.nodes = Sets.newHashSet(nodes);
  }

  public TrickleGraphBuilder() {
    this(ImmutableSet.<ConnectedNodeBuilder<?>>of());
  }

  @Override
  public <N> ChainingNodeBuilder<N, R> call(Node0<N> node) {
    ChainingNodeBuilder<N, R> nodeBuilder = new ChainingNodeBuilder<N, R>(this, node);
    nodes.add(nodeBuilder);
    return nodeBuilder;
  }

  @Override
  public <A, N> Trickle.NeedsParameters1<A, N, R> call(Node1<A, N> node) {
    ChainingNodeBuilder.NodeBuilder1<A, N, R> nodeBuilder = new ChainingNodeBuilder.NodeBuilder1<A, N, R>(this, node);
    nodes.add(nodeBuilder);

    return nodeBuilder;
  }

  @Override
  public <A, B, N> Trickle.NeedsParameters2<A, B, N, R> call(Node2<A, B, N> node) {
    ChainingNodeBuilder.NodeBuilder2<A, B, N, R> nodeBuilder = new ChainingNodeBuilder.NodeBuilder2<A, B, N, R>(this, node);
    nodes.add(nodeBuilder);

    return nodeBuilder;
  }

  @Override
  public <A, B, C, N> Trickle.NeedsParameters3<A, B, C, N, R> call(Node3<A, B, C, N> node) {
    ChainingNodeBuilder.NodeBuilder3<A, B, C, N, R> nodeBuilder = new ChainingNodeBuilder.NodeBuilder3<A, B, C, N, R>(this, node);
    nodes.add(nodeBuilder);

    return nodeBuilder;
  }

  @Override
  public ConfigureOrBuild<R> finallyCall(Node0<R> node) {
    SinkBuilder<R> nodeBuilder = new SinkBuilder<R>(this, node);
    nodes.add(nodeBuilder);
    return nodeBuilder;
  }

  @Override
  public <A> Trickle.FinalNeedsParameters1<A, R> finallyCall(Node1<A, R> node) {
    SinkBuilder.NodeBuilder1<A, R> nodeBuilder = new SinkBuilder.NodeBuilder1<A, R>(this, node);
    nodes.add(nodeBuilder);
    return nodeBuilder;
  }

  @Override
  public <A, B> Trickle.FinalNeedsParameters2<A, B, R> finallyCall(Node2<A, B, R> node) {
    SinkBuilder.NodeBuilder2<A, B, R> nodeBuilder = new SinkBuilder.NodeBuilder2<A, B, R>(this, node);
    nodes.add(nodeBuilder);
    return nodeBuilder;
  }

  @Override
  public <A, B, C> Trickle.FinalNeedsParameters3<A, B, C, R> finallyCall(Node3<A, B, C, R> node) {
    SinkBuilder.NodeBuilder3<A, B, C, R> nodeBuilder = new SinkBuilder.NodeBuilder3<A, B, C, R>(this, node);
    nodes.add(nodeBuilder);
    return nodeBuilder;
  }


  @Override
  public Graph<R> build() {
    Preconditions.checkState(!nodes.isEmpty(), "Empty graph");

    Node<R> result1 = findSink(nodes);

    return new TrickleGraph<R>(Collections.<Name<?>, Object>emptyMap(), result1, buildNodes(nodes));
  }

  private Node<R> findSink(Set<ConnectedNodeBuilder<?>> nodes) {
    final Multimap<ConnectedNodeBuilder<?>, ConnectedNodeBuilder<?>> edges = findEdges(nodes);

    Optional<Deque<ConnectedNodeBuilder<?>>> cycle = findOneCycle(edges);

    if (cycle.isPresent()) {
      throw new TrickleException("cycle detected (there may be more): " + Joiner.on(" -> ").join(cycle.get()));
    }

    Set<ConnectedNodeBuilder<?>> sinks = Sets.filter(nodes, new NoNodeDependsOn(edges));
    if (sinks.size() != 1) {
      throw new TrickleException("Multiple sinks found: " + sinks);
    }

    ConnectedNodeBuilder<?> sinkBuilder = sinks.iterator().next();

    // this cast is guaranteed to be safe by the API
    //noinspection unchecked
    return (Node<R>) sinkBuilder.getNode();
  }

  private Optional<Deque<ConnectedNodeBuilder<?>>> findOneCycle(Multimap<ConnectedNodeBuilder<?>, ConnectedNodeBuilder<?>> edges) {

    for (ConnectedNodeBuilder<?> nodeBuilder : edges.keySet()) {
      Optional<Deque<ConnectedNodeBuilder<?>>> cycle = findCycle(nodeBuilder, edges, new LinkedList<ConnectedNodeBuilder<?>>());

      if (cycle.isPresent()) {
        return cycle;
      }
    }

    return Optional.absent();
  }

  private Optional<Deque<ConnectedNodeBuilder<?>>> findCycle(ConnectedNodeBuilder<?> current, Multimap<ConnectedNodeBuilder<?>, ConnectedNodeBuilder<?>> edges, Deque<ConnectedNodeBuilder<?>> visited) {
    if (visited.contains(current)) {
      // add the current node again to close the cycle - this is not perfect, since it can lead
      // to 'cycles' like A -> B -> C -> B, but that's not really a big deal.
      visited.push(current);
      return Optional.of(visited);
    }

    visited.push(current);
    for (ConnectedNodeBuilder<?> nodeBuilder : edges.get(current)) {
      Optional<Deque<ConnectedNodeBuilder<?>>> cycle = findCycle(nodeBuilder, edges, visited);

      if (cycle.isPresent()) {
        return cycle;
      }
    }
    visited.pop();

    return Optional.absent();
  }

  private Multimap<ConnectedNodeBuilder<?>, ConnectedNodeBuilder<?>> findEdges(Set<ConnectedNodeBuilder<?>> nodes) {
    Map<Node<?>, ConnectedNodeBuilder<?>> buildersForNode = extractBuildersPerNode(nodes);

    Multimap<ConnectedNodeBuilder<?>, ConnectedNodeBuilder<?>> result = HashMultimap.create();

    for (ConnectedNodeBuilder<?> node : nodes) {
      for (Value<?> input : node.getInputs()) {
        if (input instanceof Node) {
          result.put(node, buildersForNode.get(input));
        }
      }

      for (Node<?> predecessor : node.getPredecessors()) {
        result.put(node, buildersForNode.get(predecessor));
      }
    }

    return result;
  }

  private Map<Node<?>, ConnectedNodeBuilder<?>> extractBuildersPerNode(Set<ConnectedNodeBuilder<?>> nodeBuilders) {
    Map<Node<?>, ConnectedNodeBuilder<?>> buildersForNode = Maps.newHashMap();

    for (ConnectedNodeBuilder<?> node : nodeBuilders) {
      buildersForNode.put(node.getNode(), node);
    }
    return buildersForNode;
  }


  private Map<Node<?>, ConnectedNode<?>> buildNodes(Iterable<ConnectedNodeBuilder<?>> nodeBuilders) {
    ImmutableMap.Builder<Node<?>, ConnectedNode<?>> builder = ImmutableMap.builder();

    for (ConnectedNodeBuilder<?> nodeBuilder : nodeBuilders) {
      builder.put(nodeBuilder.getNode(), nodeBuilder.connect());
    }

    return builder.build();
  }

  private static class NoNodeDependsOn implements Predicate<ConnectedNodeBuilder<?>> {
    private final Multimap<ConnectedNodeBuilder<?>, ConnectedNodeBuilder<?>> edges;

    public NoNodeDependsOn(Multimap<ConnectedNodeBuilder<?>, ConnectedNodeBuilder<?>> edges) {
      this.edges = edges;
    }

    @Override
    public boolean apply(ConnectedNodeBuilder<?> input) {
      return !edges.values().contains(input);
    }
  }
}
