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
   * Bind an input parameter to a concrete value. This means that this input value will immediately
   * be available for use.
   *
   * @param input  input to bind
   * @param value value to assign to input
   * @param <P>   type of the parameter
   * @return a new graph instance that has the value bound
   */
  public abstract <P> Graph<T> bind(Input<P> input, P value);

  /**
   * Bind an input parameter to a future value. This means that nodes using this as inputs will not
   * get invoked until the input future has completed.
   *
   * @param input  input to bind
   * @param inputFuture future for value to use
   * @param <P>   type of the parameter
   * @return a new graph instance that has the value bound
   */
  public abstract <P> Graph<T> bind(Input<P> input, ListenableFuture<P> inputFuture);

  /**
   * Run the graph, executing all node methods on the thread that completes the underlying future.
   * This is equivalent to calling {@link #run(java.util.concurrent.Executor)} with
   * {@link com.google.common.util.concurrent.MoreExecutors#sameThreadExecutor()}.
   *
   * @return a future for the value returned by the graph execution
   * @throws IllegalArgumentException if not all {@link Input}s used in node invocations are bound
   * to values
   */
  public abstract ListenableFuture<T> run();

  /**
   * Run the graph, executing node methods on the supplied executor.
   *
   * @param executor to run callbacks on
   * @return a future for the value returned by the graph execution
   * @throws IllegalArgumentException if not all {@link Input}s used in node invocations are bound
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

  /**
   * Turns debug information on or off depending on the value of the <code>debug</code> parameter.
   * The default is on (true). When debug information is on, information about intermediate states -
   * results and parameter values - for each node invocation in each graph invocation will be
   * collected and reported in case of an exception that isn't caught by a
   * {@link ConfigurableGraph#fallback(com.google.common.util.concurrent.AsyncFunction)}
   *
   * @param debug pass in <code>true</code> to turn on debug information, <code>false</code> to turn
   *              off.
   * @return a Graph instance with the specified debug setting
   */
  public abstract Graph<T> debug(boolean debug);

  // prevent construction from outside of package
  Graph() {}
}
