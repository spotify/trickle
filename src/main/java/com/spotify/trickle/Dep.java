package com.spotify.trickle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: document!
 */
abstract class Dep<T> {
  public final Class<T> cls;

  Dep(final Class<T> cls) {
    this.cls = checkNotNull(cls, "class");
  }
}
