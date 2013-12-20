package com.spotify.trickle.graph;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

/**
 * TODO: document!
 */
public class DagNode<T> {
  private final T data;
  private final Set<DagNode<T>> connectedNodes;

  public DagNode(T data, Iterable<DagNode<T>> connectedNodes) {
    this.data = data;
    this.connectedNodes = ImmutableSet.copyOf(connectedNodes);
  }

  public T getData() {
    return data;
  }

  public Set<DagNode<T>> getConnectedNodes() {
    return connectedNodes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, connectedNodes);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final DagNode other = (DagNode) obj;
    return Objects.equals(this.data, other.data) && Objects.equals(this.connectedNodes, other.connectedNodes);
  }
}
