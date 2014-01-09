package com.spotify.trickle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* TODO: document!
*/
class ValueDep<T> extends Dep<T> {
  public final T value;

  ValueDep(final T value, final Class<T> cls) {
    super(cls);
    this.value = checkNotNull(value, "value");
  }
}
