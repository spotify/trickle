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

import java.util.Objects;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Records which value was used for a given parameter during a node invocation.
 */
public class ParameterValue<T> {
  private final NodeInfo parameter;
  private final T value;

  public ParameterValue(NodeInfo parameter, @Nullable T value) {
    this.parameter = checkNotNull(parameter, "parameter");
    this.value = value;
  }

  public NodeInfo getParameter() {
    return parameter;
  }

  public T getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameter, value);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final ParameterValue other = (ParameterValue) obj;
    return Objects.equals(this.parameter, other.parameter) && Objects
        .equals(this.value, other.value);
  }

  @Override
  public String toString() {
    return "ParameterValue{" +
           "parameter=" + parameter +
           ", value=" + value +
           '}';
  }
}
