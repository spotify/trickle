package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Defines a dependency on a value bound to a specific name.
 */
class BindingDep<T> implements Dep<T> {
  private final Name<T> name;

  public BindingDep(Name<T> name) {
    this.name = checkNotNull(name, "name");
  }

  public Name<T> getName() {
    return name;
  }

  @Override
  public ListenableFuture<T> getFuture(final TraverseState state) {
    final T bindingValue = state.getBinding(name);

    checkArgument(bindingValue != null,
                  "Name not bound to a value for name %s", name);

    if (bindingValue instanceof ListenableFuture) {
      return (ListenableFuture<T>) bindingValue;
    } else {
      return immediateFuture(bindingValue);
    }
  }
}
