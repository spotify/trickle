package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public interface Node1<A1, R> extends Node<R> {
  ListenableFuture<R> run(A1 arg);
}
