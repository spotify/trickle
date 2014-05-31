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

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Defines a dependency on a value bound to a specific input.
 */
class BindingDep<T> implements Dep<T> {
  private final Input<T> input;

  public BindingDep(Input<T> input) {
    this.input = checkNotNull(input, "input");
  }

  @Override
  public ListenableFuture<T> getFuture(final TraverseState state) {
    final T bindingValue = state.getBinding(input);

    checkArgument(bindingValue != null,
                  "Input not bound to a value for input %s", input);

    if (bindingValue instanceof ListenableFuture) {
      // this cast is guaranteed by the API to be safe.
      //noinspection unchecked
      return (ListenableFuture<T>) bindingValue;
    } else {
      return immediateFuture(bindingValue);
    }
  }

  @Override
  public NodeInfo getNodeInfo() {
    return new ParameterNodeInfo();
  }


  private class ParameterNodeInfo implements NodeInfo {
    @Override
    public String name() {
      return input.getName();
    }

    @Override
    public List<? extends NodeInfo> arguments() {
      return ImmutableList.of();
    }

    @Override
    public Iterable<? extends NodeInfo> predecessors() {
      return ImmutableList.of();
    }

    @Override
    public Type type() {
      return Type.INPUT;
    }

    @Override
    public int hashCode() {
      return input.hashCode();
    }

    private Input<?> getName() {
      return input;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof BindingDep.ParameterNodeInfo)) {
        return false;
      }

      // ignoring the type parameter of the input here.
      //noinspection unchecked
      ParameterNodeInfo other = (ParameterNodeInfo) obj;

      return other.getName().equals(input);
    }

    @Override
    public String toString() {
      return input.toString();
    }
  }
}
