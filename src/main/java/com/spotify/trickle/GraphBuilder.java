package com.spotify.trickle;

/**
 * Defines the method for building a graph.
 */
public interface GraphBuilder<R> {
  Graph<R> build();
}
