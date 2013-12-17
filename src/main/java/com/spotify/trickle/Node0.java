package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public interface Node0<R> extends Node<R> {
  ListenableFuture<R> run();
}
