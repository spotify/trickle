package com.spotify.trickle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* TODO: document!
*/
class NodeDep<T> extends Dep<T> {
  public final Node<T> node;

  public NodeDep(final Node<T> node, Class<T> klazz) {
    super(klazz);
    this.node = checkNotNull(node, "node");
  }
}
