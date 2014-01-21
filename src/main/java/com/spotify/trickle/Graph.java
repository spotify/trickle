package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

/**
 * A runnable graph, possibly with unbound input parameters.
 */
public interface Graph<T> extends Value<T> {
  /**
   * Bind a parameter name to a concrete value. This means that this input value will immediately
   * be available for use.
   *
   * @param name  name to bind
   * @param value value to assign to name
   * @param <P>   type of the parameter
   * @return a new graph instance that has the value bound.
   */
  <P> Graph<T> bind(Name<P> name, P value);

  /**
   * Bind a parameter name to a future value. This means that nodes using this as inputs will not
   * get invoked until the input future has completed.
   *
   * @param name  name to bind
   * @param inputFuture future for value to use
   * @param <P>   type of the parameter
   * @return a new graph instance that has the value bound.
   */
  <P> Graph<T> bind(Name<P> name, ListenableFuture<P> inputFuture);

  /**
   * Run the graph, executing all node methods on the current thread. This is equivalent to
   * calling {@link #run(java.util.concurrent.Executor)} with
   * {@link com.google.common.util.concurrent.MoreExecutors#sameThreadExecutor()}.
   *
   * @return a future for the value returned by the graph execution.
   * @throws IllegalArgumentException if not all {@link Name}s used in node invocations are bound
   * to values
   */
  ListenableFuture<T> run();

  /**
   * Run the graph, executing node methods on the supplied executor.
   *
   * @param executor to run callbacks on
   * @return a future for the value returned by the graph execution.
   * @throws IllegalArgumentException if not all {@link Name}s used in node invocations are bound
   * to values
   */
  ListenableFuture<T> run(Executor executor);

}
