package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Code that has a inputs of type A, B and C, and returns a value of type R. Side-effects such as
 * writing to a database are permitted, but authors are encouraged to keep implementations free
 * of side-effects if at all possible.
 */
public interface Func3<A, B, C, R> extends Func<R> {
  ListenableFuture<R> run(A arg1, B arg2, C arg3);
}
