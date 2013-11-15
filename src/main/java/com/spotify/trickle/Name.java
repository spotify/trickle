/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Name<T> implements Value<T> {
  public final String name;
  private final Class<T> klazz;

  Name(String name, Class<T> klazz) {
    this.name = checkNotNull(name);
    this.klazz = klazz;
  }

  public static <T1> Name<T1> named(String name, Class<T1> klazz) {
    return new Name<>(name, klazz);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, klazz);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Name other = (Name) obj;
    return Objects.equals(this.name, other.name) && Objects.equals(this.klazz, other.klazz);
  }

  @Override
  public String toString() {
    return "Name{" + name + '}';
  }
}
