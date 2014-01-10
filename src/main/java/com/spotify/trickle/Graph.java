package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

/**
 * TODO: document!
 */
public interface Graph<T> {
  /**
   * Bind a parameter name to a concrete value.
   *
   * @param name  name to bind
   * @param value value to assign to name
   * @param <P>   type of the parameter
   * @return a new graph instance that has the value bound.
   */
  <P> Graph<T> bind(Name<P> name, P value);

  <P> Graph<T> bind(Name<P> name, ListenableFuture<P> inputFuture);

  /**
   * Run the graph, executing all callbacks on the current thread. This is equivalent to
   * calling {@link #run(java.util.concurrent.Executor)} with
   * {@link com.google.common.util.concurrent.MoreExecutors#sameThreadExecutor()}.
   *
   * @return a future for the value returned by the graph execution.
   */
  ListenableFuture<T> run();


  /**
   * Run the graph, executing callbacks on the supplied executor.
   *
   * @param executor to run callbacks on
   * @return a future for the value returned by the graph execution.
   */
  ListenableFuture<T> run(Executor executor);

}
