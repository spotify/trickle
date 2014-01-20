package com.spotify.trickle;

/**
 * Defines methods needed to build a graph of connected nodes.
 */
interface ConnectedNodeBuilder<N> {
  ConnectedNode<N> connect();
  Node<N> getNode();
  Iterable<Value<?>> getInputs();
  Iterable<Node<?>> getPredecessors();
}
