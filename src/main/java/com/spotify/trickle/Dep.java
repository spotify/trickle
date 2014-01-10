package com.spotify.trickle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract class for a dependency of an input of some class.
 *
 * TODO: do we really need the class parameter? Currently, the API guarantees type safety, so maybe
 * the internals should just ignore types entirely?
 */
abstract class Dep<T> {
  private final Class<T> cls;

  Dep(final Class<T> cls) {
    this.cls = checkNotNull(cls, "class");
  }

  public Class<T> getCls() {
    return cls;
  }
}
