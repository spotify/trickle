package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public interface Node1<A, R> extends Node<R> {
  ListenableFuture<R> run(A arg);
}
