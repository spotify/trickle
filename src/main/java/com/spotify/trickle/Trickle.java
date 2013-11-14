package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.collect.*;

import java.util.*;

/**
 * Keeping my trickle-suggestions separate for now, until we have finished discussing.
 */
public class Trickle {
  static final Object DEPENDENCY_NOT_INITIALISED = new Object();

  public static <R> GraphBuilder<R> graph(Class<R> returnClass) {
    return new GraphBuilder<>();
  }

  public static <R> GraphBuilder<R> in(Name input) {
    return new GraphBuilder<R>().inputs(input);
  }

  public static class GraphBuilder<R> {
    private final Set<Name> deps;
    private final Set<NodeBuilder<?, R>> nodes;

    private GraphBuilder(Set<Name> deps, Set<NodeBuilder<?, R>> nodes) {
      this.deps = ImmutableSet.copyOf(deps);
      this.nodes = Sets.newHashSet(nodes);
    }

    public GraphBuilder() {
      this (ImmutableSet.<Name>of(), ImmutableSet.<NodeBuilder<?, R>>of());
    }

    public <T> GraphBuilder<R> inputs(Name... dependencies) {
      ImmutableSet.Builder<Name> builder = ImmutableSet.builder();

      builder.addAll(deps);
      builder.addAll(Arrays.asList(dependencies));

      return new GraphBuilder<>(builder.build(), nodes);
    }

    public <N> NodeBuilder<N, R> call(Node0<N> node) {
      NodeBuilder<N, R> nodeBuilder = new NodeBuilder<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public <A1, N> NodeBuilder<N, R> call(Node1<A1, N> node) {
      NodeBuilder<N, R> nodeBuilder = new NodeBuilder<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public <A1,A2,N> NodeBuilder<N, R> call(Node2<A1, A2, N> node) {
      NodeBuilder<N, R> nodeBuilder = new NodeBuilder<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public <A1,A2,A3,N> NodeBuilder<N, R> call(Node3<A1, A2, A3, N> node) {
      NodeBuilder<N, R> nodeBuilder = new NodeBuilder<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public TrickleGraph<R> out(TNode<R> result) {
      Map<Name,Object> inputDependencies =
          Maps.asMap(deps, new Function<Name, Object>() {
            @Override
            public Object apply(Name input) {
              return DEPENDENCY_NOT_INITIALISED;
            }
          });

      return new TrickleGraph<>(inputDependencies, result, buildNodes(nodes));
    }

    private Map<TNode<?>, ConnectedNode> buildNodes(Iterable<NodeBuilder<?, R>> nodeBuilders) {
      ImmutableMap.Builder<TNode<?>, ConnectedNode> builder = ImmutableMap.builder();

      for (NodeBuilder<?, R> nodeBuilder : nodeBuilders) {
        builder.put(nodeBuilder.node, nodeBuilder.connect());
      }

      return builder.build();
    }
  }


  public static class NodeBuilder<N, R> {
    private final GraphBuilder<R> graphBuilder;
    private final TNode<N> node;
    private final List<Object> inputs;
    private final List<TNode<?>> predecessors;

    private NodeBuilder(GraphBuilder<R> graphBuilder, TNode<N> node) {
      this.graphBuilder = graphBuilder;
      this.node = node;
      inputs = new ArrayList<>();
      predecessors = new ArrayList<>();
    }

    public NodeBuilder<N, R> with(Object... inputs) {
      this.inputs.addAll(Arrays.asList(inputs));
      return this;
    }

    public <N1> NodeBuilder<N1, R> call(Node0<N1> put1) {
      return graphBuilder.call(put1);
    }

    public <A1, N1> NodeBuilder<N1, R> call(Node1<A1, N1> put1) {
      return graphBuilder.call(put1);
    }

    public <A1, A2, N1> NodeBuilder<N1, R> call(Node2<A1, A2, N1> put1) {
      return graphBuilder.call(put1);
    }

    public <A1, A2, A3, N1> NodeBuilder<N1, R> call(Node3<A1, A2, A3, N1> put1) {
      return graphBuilder.call(put1);
    }

    public NodeBuilder<N, R> after(TNode<?>... predecessors) {
      this.predecessors.addAll(Arrays.asList(predecessors));
      return this;
    }

    public TrickleGraph<R> output(TNode<R> result1) {
      return graphBuilder.out(result1);
    }

    private ConnectedNode connect() {
      List<Dep<?>> deps = asDeps(inputs);
      return new ConnectedNode(
          node,
          deps,
          predecessors,
          Transformers.newNoChecksTransformer(deps, node)
      );
    }

    private List<Dep<?>> asDeps(List<Object> inputs) {
      List<Dep<?>> result = Lists.newArrayList();

      for (Object input : inputs) {
        if (input instanceof Name) {
          result.add(new BindingDep<>((Name) input, Object.class));
        }
        else if (input instanceof TNode<?>) {
          result.add(new NodeDep((TNode<?>) input));
        }
        else {
          throw new RuntimeException("illegal input object: " + input);
        }
      }

      return result;
    }
  }
}
