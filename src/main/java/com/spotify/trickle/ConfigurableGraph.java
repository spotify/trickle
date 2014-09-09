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

import com.google.common.util.concurrent.AsyncFunction;

/**
 * Defines operations available on an intermediate node builder when constructing a graph.
 */
public abstract class ConfigurableGraph<R> extends Graph<R> {

  /**
   * Adds a fallback that is executed if any node leading up to the current one throws an exception.
   * In case of an exception during graph execution, the value returned by the {@code handler}
   * function will be returned instead of a failed future. If the handler throws an exception, a
   * failed future will be returned, just as if there was no handler in the first place.
   *
   * @param handler a function whose value is used as fallback in case of an exception
   */
  public abstract ConfigurableGraph<R> fallback(AsyncFunction<Throwable, R> handler);

  /**
   * Give this node a name for use in troubleshooting.
   *
   * @param name the name of the current node
   */
  public abstract ConfigurableGraph<R> named(String name);

  /**
   * Indicates that this node should not be executed until the predecessor nodes have been executed.
   * If the graph already had a set of predecessors, the sets will be merged.
   *
   * @param predecessors a set of graphs to add as predecessors
   */
  public abstract ConfigurableGraph<R> after(Graph<?>... predecessors);

  // prevent construction from outside of package
  ConfigurableGraph() {
  }
}
