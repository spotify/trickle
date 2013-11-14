package com.spotify.trickle;

/**
* TODO: document!
*/
class NodeDep extends Dep<Object> {
  public final TNode<?> node;

  public NodeDep(final TNode<?> node) {
    super(Object.class);
    this.node = node;
  }
}
