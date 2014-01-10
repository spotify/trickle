package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A node that has no inputs.
 */
public interface Node0<R> extends Node<R> {
  ListenableFuture<R> run();
}
