package com.spotify.trickle;

import javax.annotation.Nullable;

/**
 * Indicates that there was a consistency error when setting up the Trickle graph.
 */
public class TrickleException extends RuntimeException {
  public TrickleException(@Nullable String message) {
    super(message);
  }
}
