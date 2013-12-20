package com.spotify.trickle.graph;

import java.util.Set;

/**
 * TODO: document!
 */
public interface DagChecker {
  <T> Set<DagNode<T>> findSinks(Set<DagNode<T>> dag);
}
