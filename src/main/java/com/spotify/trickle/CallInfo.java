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

import java.util.List;
import java.util.Objects;

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

  @Override
  public int hashCode() {
    return Objects.hash(nodeInfo, parameterValues);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final CallInfo other = (CallInfo) obj;
    return Objects.equals(this.nodeInfo, other.nodeInfo) && Objects
        .equals(this.parameterValues, other.parameterValues);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("call ");
    builder.append(nodeInfo);
    builder.append("(");

    boolean first = true;
    for (ParameterValue<?> parameterValue : parameterValues) {
      if (!first) {
        builder.append(", ");
      }
      first = false;
      builder.append(parameterValue.getParameter());
      builder.append("=");
      builder.append(parameterValue.getValue());
    }
    builder.append(")");

    return builder.toString();
  }
}
