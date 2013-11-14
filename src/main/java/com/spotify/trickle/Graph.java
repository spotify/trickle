package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * TODO: document!
 */
public interface Graph<T> {
  <P> Graph<T> bind(Name input, P value);

  ListenableFuture<T> run();
}
