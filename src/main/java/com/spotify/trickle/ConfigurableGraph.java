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
  public abstract ConfigurableGraph<R> fallback(AsyncFunction<Throwable, R> handler);

  public abstract ConfigurableGraph<R> named(String name);

  public abstract ConfigurableGraph<R> after(Graph<?>... predecessors);

  // prevent construction from outside of package
  ConfigurableGraph() {}
}
