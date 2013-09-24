package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.spotify.trickle.transform.Transformers;

import java.util.*;

/**
 * Keeping my trickle-suggestions separate for now, until we have finished discussing.
 */
public class PTrickle {
  static final Object DEPENDENCY_NOT_INITIALISED = new Object();

  public static <R> GraphBuilder<R> graph(Class<R> returnClass) {
    return new GraphBuilder<>();
  }

  public static <R> GraphBuilder<R> in(Name input) {
    return new GraphBuilder<R>().in(input);
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

    public <T> GraphBuilder<R> in(Name... dependencies) {
      ImmutableSet.Builder<Name> builder = ImmutableSet.builder();

      builder.addAll(deps);
      builder.addAll(Arrays.asList(dependencies));

      return new GraphBuilder<>(builder.build(), nodes);
    }

    public <N> NodeBuilder<N, R> call(PNode<N> node) {
      NodeBuilder<N, R> nodeBuilder = new NodeBuilder<>(this, node);
      nodes.add(nodeBuilder);

      return nodeBuilder;
    }

    public Graph<R> out(PNode<R> result) {
      Map<Name,Object> inputDependencies =
          Maps.asMap(deps, new Function<Name, Object>() {
            @Override
            public Object apply(Name input) {
              return DEPENDENCY_NOT_INITIALISED;
            }
          });

      return new Graph<>(inputDependencies, result, buildNodes(nodes));
    }

    private Map<PNode<?>, ConnectedNode> buildNodes(Iterable<NodeBuilder<?, R>> nodeBuilders) {
      ImmutableMap.Builder<PNode<?>, ConnectedNode> builder = ImmutableMap.builder();

      for (NodeBuilder<?, R> nodeBuilder : nodeBuilders) {
        builder.put(nodeBuilder.node, nodeBuilder.connect());
      }

      return builder.build();
    }
  }




  public static class NodeBuilder<N, R> {
    private final GraphBuilder<R> graphBuilder;
    private final PNode<N> node;
    private final List<Object> inputs;
    private final List<PNode<?>> predecessors;

    public NodeBuilder(GraphBuilder<R> graphBuilder, PNode<N> node) {
      this.graphBuilder = graphBuilder;
      this.node = node;
      inputs = new ArrayList<>();
      predecessors = new ArrayList<>();
    }

    public NodeBuilder<N, R> with(Object... inputs) {
      this.inputs.addAll(Arrays.asList(inputs));
      return this;
    }

    public <N1> NodeBuilder<N1, R> call(PNode<N1> put1) {
      return graphBuilder.call(put1);
    }

    public NodeBuilder<N, R> after(PNode<?>... predecessors) {
      this.predecessors.addAll(Arrays.asList(predecessors));
      return this;
    }

    public Graph<R> out(PNode<R> result1) {
      return graphBuilder.out(result1);
    }

    public ConnectedNode connect() {
      List<Trickle.Dep<?>> deps = asDeps(inputs);
      return new ConnectedNode(
          node,
          deps,
          predecessors,
          Transformers.newNoChecksTransformer(deps, node.getNodeObject())
      );
    }

    private List<Trickle.Dep<?>> asDeps(List<Object> inputs) {
      List<Trickle.Dep<?>> result = Lists.newArrayList();

      for (Object input : inputs) {
        if (input instanceof Name) {
          result.add(new Trickle.BindingDep<>((Name) input, Object.class));
        }
        else if (input instanceof PNode<?>) {
          result.add(new Trickle.PNodeDep((PNode<?>) input));
        }
        else {
          throw new RuntimeException("illegal input object: " + input);
        }
      }

      return result;
    }
  }
}
