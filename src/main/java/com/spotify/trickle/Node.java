package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public class Node<T> {
  private final NodeMethod<T> nodeObject;

  public Node(NodeMethod<T> nodeObject) {
    this.nodeObject = nodeObject;
  }

  public NodeMethod<T> getNodeObject() {
    return nodeObject;
  }

  public static <V> Node<V> of(NodeMethod<V> nodeObject) {
    return new Node<>(nodeObject);
  }

  public static interface NodeMethod<T> {
    ListenableFuture<T> run(Object... args);
  }
}
