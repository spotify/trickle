package com.spotify.trickle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines a dependency of a parameter that is a node.
 */
class NodeDep<T> extends Dep<T> {
  private final Node<?> node;

  public NodeDep(final Node<?> node, Class<T> klazz) {
    super(klazz);
    this.node = checkNotNull(node, "node");
  }

  public Node<?> getNode() {
    return node;
  }
}
