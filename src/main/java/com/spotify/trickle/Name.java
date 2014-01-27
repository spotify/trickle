/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.base.Objects;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines a named value of some specific class. Before a graph can be executed, all used names
 * must be bound to concrete values ({@link Graph#bind(Name, Object)}) or future values
 * ({@link Graph#bind(Name, com.google.common.util.concurrent.ListenableFuture)}).
 *
 * @param <T> the type of values with this name
 */
public final class Name<T> implements Parameter<T> {
  private final String name;

  Name(String name) {
    this.name = checkNotNull(name);
  }

  /**
   * Factory method for a name.
   *
   * @param name String identifier for the name. Must be unique within a single {@link Graph}
   * @return a Name instance
   */
  public static <U> Name<U> named(String name) {
    return new Name<U>(name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
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
    return Objects.equal(this.name, other.name);
  }

  @Override
  public String toString() {
    return "Name{" + getName() + '}';
  }

  public String getName() {
    return name;
  }
}
