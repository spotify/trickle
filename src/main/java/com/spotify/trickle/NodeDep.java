package com.spotify.trickle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: document!
 */
class NodeDep<T> extends Dep<T> {
  private final Node<T> node;

  public NodeDep(final Node<T> node, Class<T> klazz) {
    super(klazz);
    this.node = checkNotNull(node, "node");
  }

  public Node<T> getNode() {
    return node;
  }
}
