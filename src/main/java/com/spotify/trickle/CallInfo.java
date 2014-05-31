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
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contains troubleshooting information about a node invocation that was made during a graph
 * execution.
 */
public class CallInfo {
  private final NodeInfo nodeInfo;
  private final List<ParameterValue<?>> parameterValues;

  public CallInfo(NodeInfo nodeInfo, List<ParameterValue<?>> parameterValues) {
    this.nodeInfo = checkNotNull(nodeInfo, "nodeInfo");
    this.parameterValues = ImmutableList.copyOf(parameterValues);
  }

  public NodeInfo getNodeInfo() {
    return nodeInfo;
  }

  public List<ParameterValue<?>> getParameterValues() {
    return parameterValues;
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

    CallInfo callInfo = (CallInfo) o;

    if (!nodeInfo.equals(callInfo.nodeInfo)) {
      return false;
    }
    if (!parameterValues.equals(callInfo.parameterValues)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = nodeInfo.hashCode();
    result = 31 * result + parameterValues.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("nodeInfo", nodeInfo)
        .add("parameterValues", parameterValues)
        .toString();
  }
}
