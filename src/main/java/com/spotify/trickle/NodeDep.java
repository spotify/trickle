package com.spotify.trickle;

/**
* TODO: document!
*/
class NodeDep<T> extends Dep<T> {
  public final Node<T> node;

  public NodeDep(final Node<T> node, Class<T> klazz) {
    super(klazz);
    this.node = node;
  }
}
