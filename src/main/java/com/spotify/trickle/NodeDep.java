package com.spotify.trickle;

/**
* TODO: document!
*/
class NodeDep extends Dep<Object> {
  public final Node<?> node;

  public NodeDep(final Node<?> node) {
    super(Object.class);
    this.node = node;
  }
}
