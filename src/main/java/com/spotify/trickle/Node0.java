package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public interface Node0<R> extends TNode<R> {
  ListenableFuture<R> run();
}
