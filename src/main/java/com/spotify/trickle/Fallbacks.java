package com.spotify.trickle;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;

import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Contains utility methods for dealing with fallbacks.
 */
public final class Fallbacks {
  private Fallbacks() {
    // prevent instantiation
  }

  public static <T> AsyncFunction<Throwable, T> always(@Nullable final T value) {
    return new AsyncFunction<Throwable, T>() {
      @Nullable
      @Override
      public ListenableFuture<T> apply(@Nullable Throwable input) {
        return immediateFuture(value);
      }
    };
  }
}
