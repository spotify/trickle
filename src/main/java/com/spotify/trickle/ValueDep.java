package com.spotify.trickle;

/**
* TODO: document!
*/
class ValueDep<T> extends Dep<T> {
  public final T value;

  ValueDep(final T value, final Class<T> cls) {
    super(cls);
    this.value = value;
  }
}
