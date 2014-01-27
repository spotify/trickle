package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Code that has no inputs and returns a value of type R. Side-effects such as
 * writing to a database are permitted, but authors are encouraged to keep implementations free
 * of side-effects if at all possible.
 */
public interface Func0<R> extends Func<R> {
  ListenableFuture<R> run();
}
