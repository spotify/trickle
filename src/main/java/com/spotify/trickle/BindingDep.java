package com.spotify.trickle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: document!
 */
class BindingDep<T> extends Dep<T> {
  public final Name<?> name;

  public BindingDep(Name<?> name, Class<T> cls) {
    super(cls);
    this.name = checkNotNull(name, "name");
  }
}
