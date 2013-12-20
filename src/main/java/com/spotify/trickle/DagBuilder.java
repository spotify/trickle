package com.spotify.trickle;

import com.google.common.collect.Sets;
import com.spotify.trickle.graph.DagNode;

import java.util.Set;

/**
 * TODO: document!
 */
public class DagBuilder {
  public static <R> Set<DagNode<Trickle.NodeBuilder<?, R>>> buildDag(Set<Trickle.NodeBuilder<?, R>> nodes) {
    Set<Trickle.NodeBuilder<?, R>> visited = Sets.newHashSet();
    Set<DagNode<Trickle.NodeBuilder<?, R>>> built = Sets.newHashSet();

    for (Trickle.NodeBuilder<?, R> builder: nodes) {
      if (!visited.contains(builder)) {
        recursiveBuildNode(builder, built, visited);
      }
    }

    return built;
  }

  private static <R> void recursiveBuildNode(Trickle.NodeBuilder<?, R> builder, Set<DagNode<Trickle.NodeBuilder<?, R>>> built, Set<Trickle.NodeBuilder<?, R>> visited) {
    // blah
  }
}
