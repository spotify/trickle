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
* TODO: document!
*/
final class TrickleGraphBuilder<R> implements GraphBuilder<R>, NodeChainer<R> {
  private final Set<TrickleNodeBuilder<?, R>> nodes;

  TrickleGraphBuilder(Set<TrickleNodeBuilder<?, R>> nodes) {
    this.nodes = Sets.newHashSet(nodes);
  }

  public TrickleGraphBuilder() {
    this(ImmutableSet.<TrickleNodeBuilder<?, R>>of());
  }

  @Override
  public <N> TrickleNodeBuilder<N, R> call(Node0<N> node) {
    TrickleNodeBuilder<N, R> nodeBuilder = new TrickleNodeBuilder<>(this, node);
    nodes.add(nodeBuilder);

    return nodeBuilder;
  }

  @Override
  public <A, N> Trickle.NeedsParameters1<A, N, R> call(Node1<A, N> node) {
    TrickleNodeBuilder.NodeBuilder1<A, N, R> nodeBuilder = new TrickleNodeBuilder.NodeBuilder1<>(this, node);
    nodes.add(nodeBuilder);

    return nodeBuilder;
  }

  @Override
  public <A, B, N> Trickle.NeedsParameters2<A, B, N, R> call(Node2<A, B, N> node) {
    TrickleNodeBuilder.NodeBuilder2<A, B, N, R> nodeBuilder = new TrickleNodeBuilder.NodeBuilder2<>(this, node);
    nodes.add(nodeBuilder);

    return nodeBuilder;
  }

  @Override
  public <A, B, C, N> Trickle.NeedsParameters3<A, B, C, N, R> call(Node3<A, B, C, N> node) {
    TrickleNodeBuilder.NodeBuilder3<A, B, C, N, R> nodeBuilder = new TrickleNodeBuilder.NodeBuilder3<>(this, node);
    nodes.add(nodeBuilder);

    return nodeBuilder;
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
  public Graph<R> build() {
    Preconditions.checkState(!nodes.isEmpty(), "Empty graph");

    Node<R> result1 = findSink(nodes);

    return new TrickleGraph<>(Collections.<Name<?>, Object>emptyMap(), result1, buildNodes(nodes));
  }

  private Node<R> findSink(Set<TrickleNodeBuilder<?, R>> nodes) {
    final Multimap<TrickleNodeBuilder<?, R>, TrickleNodeBuilder<?, R>> edges = findEdges(nodes);

    Optional<Deque<TrickleNodeBuilder<?, R>>> cycle = findOneCycle(edges);

    if (cycle.isPresent()) {
      throw new TrickleException("cycle detected (there may be more): " + Joiner.on(" -> ").join(cycle.get()));
    }

    Set<TrickleNodeBuilder<?, R>> sinks = Sets.filter(nodes, new NoNodeDependsOn<>(edges));
    if (sinks.size() != 1) {
      throw new TrickleException("Multiple sinks found: " + sinks);
    }

    TrickleNodeBuilder<?, R> sinkBuilder = sinks.iterator().next();

    // note that there is no guarantee that this cast is safe. That's bad, but I'm not sure what
    // to do about it. TODO: think about this
    return (Node<R>) sinkBuilder.getNode();
  }

  private Optional<Deque<TrickleNodeBuilder<?, R>>> findOneCycle(Multimap<TrickleNodeBuilder<?, R>, TrickleNodeBuilder<?, R>> edges) {

    for (TrickleNodeBuilder<?, R> nodeBuilder : edges.keySet()) {
      Optional<Deque<TrickleNodeBuilder<?, R>>> cycle = findCycle(nodeBuilder, edges, new LinkedList<TrickleNodeBuilder<?, R>>());

      if (cycle.isPresent()) {
        return cycle;
      }
    }

    return Optional.absent();
  }

  private Optional<Deque<TrickleNodeBuilder<?, R>>> findCycle(TrickleNodeBuilder<?, R> current, Multimap<TrickleNodeBuilder<?, R>, TrickleNodeBuilder<?, R>> edges, Deque<TrickleNodeBuilder<?, R>> visited) {
    if (visited.contains(current)) {
      // add the current node again to close the cycle - this is not perfect, since it can lead
      // to 'cycles' like A -> B -> C -> B, but that's not really a big deal.
      visited.push(current);
      return Optional.of(visited);
    }

    visited.push(current);
    for (TrickleNodeBuilder<?, R> nodeBuilder : edges.get(current)) {
      Optional<Deque<TrickleNodeBuilder<?, R>>> cycle = findCycle(nodeBuilder, edges, visited);

      if (cycle.isPresent()) {
        return cycle;
      }
    }
    visited.pop();

    return Optional.absent();
  }

  private Multimap<TrickleNodeBuilder<?, R>, TrickleNodeBuilder<?, R>> findEdges(Set<TrickleNodeBuilder<?, R>> nodes) {
    Map<Node<?>, TrickleNodeBuilder<?, R>> buildersForNode = extractBuildersPerNode(nodes);

    Multimap<TrickleNodeBuilder<?, R>, TrickleNodeBuilder<?, R>> result = HashMultimap.create();

    for (TrickleNodeBuilder<?, R> node : nodes) {
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

  private Map<Node<?>, TrickleNodeBuilder<?, R>> extractBuildersPerNode(Set<TrickleNodeBuilder<?, R>> nodeBuilders) {
    Map<Node<?>, TrickleNodeBuilder<?, R>> buildersForNode = Maps.newHashMap();

    for (TrickleNodeBuilder<?, R> node : nodeBuilders) {
      buildersForNode.put(node.getNode(), node);
    }
    return buildersForNode;
  }


  private Map<Node<?>, ConnectedNode<?>> buildNodes(Iterable<TrickleNodeBuilder<?, R>> nodeBuilders) {
    ImmutableMap.Builder<Node<?>, ConnectedNode<?>> builder = ImmutableMap.builder();

    for (TrickleNodeBuilder<?, R> nodeBuilder : nodeBuilders) {
      builder.put(nodeBuilder.getNode(), nodeBuilder.connect());
    }

    return builder.build();
  }

  private static class NoNodeDependsOn<R> implements Predicate<TrickleNodeBuilder<?, R>> {
    private final Multimap<TrickleNodeBuilder<?, R>, TrickleNodeBuilder<?, R>> edges;

    public NoNodeDependsOn(Multimap<TrickleNodeBuilder<?, R>, TrickleNodeBuilder<?, R>> edges) {
      this.edges = edges;
    }

    @Override
    public boolean apply(TrickleNodeBuilder<?, R> input) {
      return !edges.values().contains(input);
    }
  }
}
