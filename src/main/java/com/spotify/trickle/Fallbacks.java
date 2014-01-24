package com.spotify.trickle;

import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Contains utility methods for dealing with fallbacks.
 */
public final class Fallbacks {
  private Fallbacks() {
    // prevent instantiation
  }

  public static <T> Function<Throwable, T> always(@Nullable final T value) {
    return new Function<Throwable, T>() {
      @Nullable
      @Override
      public T apply(@Nullable Throwable input) {
        return value;
      }
    };
  }
}
