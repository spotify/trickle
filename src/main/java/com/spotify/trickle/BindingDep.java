package com.spotify.trickle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines a dependency on a value bound to a specific name.
 */
class BindingDep<T> extends Dep<T> {
  private final Name<?> name;

  public BindingDep(Name<?> name, Class<T> cls) {
    super(cls);
    this.name = checkNotNull(name, "name");
  }

  public Name<?> getName() {
    return name;
  }
}
