package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public interface Node2<A1, A2, R> extends Node<R> {
  ListenableFuture<R> run(A1 arg1, A2 arg2);
}
