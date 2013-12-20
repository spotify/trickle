package com.spotify.trickle.graph;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * TODO: document!
 */
public class SimpleDagChecker implements DagChecker {
  @Override
  public <T> Set<DagNode<T>> findSinks(Set<DagNode<T>> dag) {
    ImmutableSet.Builder<DagNode<T>> builder = ImmutableSet.builder();

    for (DagNode<T> node : dag) {
      if (node.getConnectedNodes().isEmpty()) {
        builder.add(node);
      }
    }

    return builder.build();
  }
}
