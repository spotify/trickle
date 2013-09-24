package com.spotify.trickle;

/**
 * TODO: document!
 */
public class PNode<T> {
  private final Object nodeObject;

  public PNode(Object nodeObject) {
    this.nodeObject = nodeObject;
  }

  public Object getNodeObject() {
    return nodeObject;
  }

  public static <V> PNode<V> of(Object nodeObject) {
    return new PNode<>(nodeObject);
  }
}
