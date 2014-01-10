package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public interface Node2<A, B, R> extends Node<R> {
  ListenableFuture<R> run(A arg1, B arg2);
}
