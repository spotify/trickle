package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public interface Node3<A1, A2, A3, R> extends Node<R> {
  ListenableFuture<R> run(A1 arg1, A2 arg2, A3 arg3);
}
