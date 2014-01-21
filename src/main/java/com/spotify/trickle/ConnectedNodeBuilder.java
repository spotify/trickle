package com.spotify.trickle;

/**
 * Defines methods needed to build a graph of connected nodes.
 */
interface ConnectedNodeBuilder<R> {
  ConnectedNode<R> connect();
}
