package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public interface Node3<A, B, C, R> extends Node<R> {
  ListenableFuture<R> run(A arg1, B arg2, C arg3);
}
