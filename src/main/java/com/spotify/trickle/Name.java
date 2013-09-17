/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Name {
  public final String name;

  Name(String name) {
    this.name = checkNotNull(name);
  }

  public static Name named(String name) {
    return new Name(name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Name name1 = (Name) o;

    if (!name.equals(name1.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "Name{" + name + '}';
  }
}
