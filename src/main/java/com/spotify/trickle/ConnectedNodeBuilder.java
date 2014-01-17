package com.spotify.trickle;

/**
 * TODO: document!
 */
interface ConnectedNodeBuilder<N> {
  ConnectedNode<N> connect();
  Node<N> getNode();
  Iterable<Value<?>> getInputs();
  Iterable<Node<?>> getPredecessors();
}
