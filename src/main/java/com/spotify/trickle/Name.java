/*
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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
