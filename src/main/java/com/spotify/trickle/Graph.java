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

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

/**
 * A runnable graph, possibly with unbound input parameters.
 */
public abstract class Graph<T> implements Parameter<T>, NodeInfo {

  /**
   * Bind a parameter name to a concrete value. This means that this input value will immediately
   * be available for use.
   *
   * @param name  name to bind
   * @param value value to assign to name
   * @param <P>   type of the parameter
   * @return a new graph instance that has the value bound
   */
  public abstract <P> Graph<T> bind(Name<P> name, P value);

  /**
   * Bind a parameter name to a future value. This means that nodes using this as inputs will not
   * get invoked until the input future has completed.
   *
   * @param name  name to bind
   * @param inputFuture future for value to use
   * @param <P>   type of the parameter
   * @return a new graph instance that has the value bound
   */
  public abstract <P> Graph<T> bind(Name<P> name, ListenableFuture<P> inputFuture);

  /**
   * Run the graph, executing all node methods on the current thread. This is equivalent to
   * calling {@link #run(java.util.concurrent.Executor)} with
   * {@link com.google.common.util.concurrent.MoreExecutors#sameThreadExecutor()}.
   *
   * @return a future for the value returned by the graph execution
   * @throws IllegalArgumentException if not all {@link Name}s used in node invocations are bound
   * to values
   */
  public abstract ListenableFuture<T> run();

  /**
   * Run the graph, executing node methods on the supplied executor.
   *
   * @param executor to run callbacks on
   * @return a future for the value returned by the graph execution
   * @throws IllegalArgumentException if not all {@link Name}s used in node invocations are bound
   * to values
   */
  public abstract ListenableFuture<T> run(Executor executor);

  /**
   * Package private method for running the graph from an existing state.
   *
   * @param state  state to continue running from
   * @return future for the value returned by the graph execution
   */
  abstract ListenableFuture<T> run(TraverseState state);

  // prevent construction from outside of package
  Graph() {}
}
