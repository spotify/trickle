package com.spotify.trickle;

/**
* TODO: document!
*/
class BindingDep<T> extends Dep<T> {
  public final Name name;

  public BindingDep(Name name, Class<T> cls) {
    super(cls);
    this.name = name;
  }
}
