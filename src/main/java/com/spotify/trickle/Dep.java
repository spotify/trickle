package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Interface for a dependency of an input.
 */
interface Dep<T> extends GraphElement {
  ListenableFuture<T> getFuture(TraverseState state);
}
