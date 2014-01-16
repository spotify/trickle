package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines a dependency of a parameter that is a node.
 */
class NodeDep<T> implements Dep<T> {
  private final Node<T> node;

  public NodeDep(final Node<T> node) {
    this.node = checkNotNull(node, "node");
  }

  public Node<T> getNode() {
    return node;
  }

  @Override
  public ListenableFuture<T> getFuture(final TraverseState state) {
    return state.futureForNode(node);
  }
}
