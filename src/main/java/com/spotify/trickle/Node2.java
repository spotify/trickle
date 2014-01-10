package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A node that takes two inputs of type A and B.
 */
public interface Node2<A, B, R> extends Node<R> {
  ListenableFuture<R> run(A arg1, B arg2);
}
