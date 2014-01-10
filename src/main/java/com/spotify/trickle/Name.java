/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines a named value of some specific class. Before a graph can be executed, all used names
 * must be bound to concrete values ({@link Graph#bind(Name, Object)}) or future values
 * ({@link Graph#bind(Name, com.google.common.util.concurrent.ListenableFuture)}).
 *
 * @param <T> the type of values with this name
 */
public final class Name<T> implements Value<T> {
  private final String name;
  private final Class<T> klazz;

  Name(String name, Class<T> klazz) {
    this.name = checkNotNull(name);
    this.klazz = checkNotNull(klazz, "class");
  }

  /**
   * Factory method for a name.
   *
   * @param name String identifier for the name. Must be unique within a single {@link Graph}
   * @param klazz the type of values with this name
   * @return a Name instance
   */
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
