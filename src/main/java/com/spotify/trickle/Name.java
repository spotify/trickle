/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Name<T> implements Value<T> {
  private final String name;
  private final Class<T> klazz;

  Name(String name, Class<T> klazz) {
    this.name = checkNotNull(name);
    this.klazz = checkNotNull(klazz, "class");
  }

  public static <U> Name<U> named(String name, Class<U> klazz) {
    return new Name<>(name, klazz);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), klazz);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Name other = (Name) obj;
    return Objects.equals(this.getName(), other.getName()) && Objects.equals(this.klazz, other.klazz);
  }

  @Override
  public String toString() {
    return "Name{" + getName() + '}';
  }

  public String getName() {
    return name;
  }
}
