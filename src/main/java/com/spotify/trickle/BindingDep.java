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
 * Defines a dependency on a value bound to a specific name.
 */
class BindingDep<T> implements Dep<T> {
  private final Name<T> name;

  public BindingDep(Name<T> name) {
    this.name = checkNotNull(name, "name");
  }

  @Override
  public ListenableFuture<T> getFuture(final TraverseState state) {
    final T bindingValue = state.getBinding(name);

    checkArgument(bindingValue != null,
                  "Name not bound to a value for name %s", name);

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
      return name.getName();
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
      return Type.PARAMETER;
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

    private Name<?> getName() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof BindingDep.ParameterNodeInfo)) {
        return false;
      }

      // ignoring the type parameter of the name here.
      //noinspection unchecked
      ParameterNodeInfo other = (ParameterNodeInfo) obj;

      return other.getName().equals(name);
    }
  }
}
