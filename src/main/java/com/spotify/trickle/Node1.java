package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A node that has a single input of type A.
 */
public interface Node1<A, R> extends Node<R> {
  ListenableFuture<R> run(A arg);
}
