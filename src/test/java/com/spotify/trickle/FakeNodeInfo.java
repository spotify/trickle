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

import java.util.List;

import static java.util.Collections.emptyList;

class FakeNodeInfo implements NodeInfo {
  private final String name;
  private final List<? extends NodeInfo> arguments;

  FakeNodeInfo(String name, List<? extends NodeInfo> arguments) {
    this.name = name;
    this.arguments = arguments;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public List<? extends NodeInfo> arguments() {
    return arguments;
  }

  @Override
  public Iterable<? extends NodeInfo> predecessors() {
    return emptyList();
  }

  @Override
  public Type type() {
    return Type.NODE;
  }

  @Override
  public String toString() {
    return name;
  }
}
