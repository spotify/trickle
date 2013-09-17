/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle.transform;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

public interface Transformer<T> {
  /**
   * Builds a transformation from a list of values.
   *
   * The values can either be futures or direct values. The
   * doneSignal is a future that signals when all the
   * futures in the values list are ready to be read.
   *
   * @param values      A list of values as either futures or
   *                    direct values.
   * @param doneSignal  A future signaling when all futures
   *                    in the values list are complete.
   * @return  A ListenableFuture of the transformation.
   */
  ListenableFuture<T> createTransform(
      ImmutableList<Object> values,
      ListenableFuture<?> doneSignal);
}
