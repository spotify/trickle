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

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ParameterValue that = (ParameterValue) o;

    if (!parameter.equals(that.parameter)) {
      return false;
    }
    if (value != null ? !value.equals(that.value) : that.value != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = parameter.hashCode();
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("parameter", parameter)
        .add("value", value)
        .toString();
  }
}
