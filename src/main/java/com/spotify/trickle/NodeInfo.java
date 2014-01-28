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

/**
 * Interface defining the debug information that is available about a Trickle node or input.
 */
public interface NodeInfo {
  /**
   * @return the node name - defaults to "unnamed"
   */
  String name();

  /**
   * Gets the ordered list of node arguments.
   *
   * @return the possibly empty list of node arguments.
   */
  List<? extends NodeInfo> arguments();

  /**
   * Gets the possibly empty, unordered collection of nodes that must be completed before this
   * one can be executed.
   *
   * @return the collection of predecessors
   */
  Iterable<? extends NodeInfo> predecessors();

  /**
   * Returns the node type:
   * <ul>
   *   <li>PARAMETER = this is a parameter to the graph as a whole.</li>
   *   <li>NODE = this is a node in the graph</li>
   * </ul>
   * @return the node type
   */
  Type type();

  public static enum Type {
    PARAMETER,
    NODE
  }
}
