package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

/**
 * TODO: document!
 */
public interface Graph<T> {
  <P> Graph<T> bind(Name input, P value);

  ListenableFuture<T> run();
  ListenableFuture<T> run(Executor executor);
}
