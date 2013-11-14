package com.spotify.trickle;

/**
* TODO: document!
*/
abstract class Dep<T> {
  public final Class<T> cls;

  Dep(final Class<T> cls) {
    this.cls = cls;
  }
}
