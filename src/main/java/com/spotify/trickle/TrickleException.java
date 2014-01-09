package com.spotify.trickle;

import javax.annotation.Nullable;

/**
 * TODO: document!
 */
public class TrickleException extends RuntimeException {
  public TrickleException(@Nullable String message) {
    super(message);
  }
}
