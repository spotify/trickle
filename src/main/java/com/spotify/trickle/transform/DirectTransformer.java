/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle.transform;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.util.concurrent.Futures.getUnchecked;
import static com.google.common.util.concurrent.Futures.transform;

class DirectTransformer<T> implements Transformer<T> {

  DirectTransformer() {}

  @Override
  public ListenableFuture<T> createTransform(
      final ImmutableList<Object> values,
      final ListenableFuture<?> doneSignal) {

    return transform(doneSignal, new Function<Object, T>() {
      @Override
      public T apply(Object _) {
        return getValue(values);
      }
    });
  }

  private T getValue(final ImmutableList<Object> values) {
    final Object valueObject = values.get(0);

    Object value;
    if (valueObject instanceof ListenableFuture<?>) {
      value = getUnchecked((ListenableFuture<?>) valueObject);
    } else {
      value = valueObject;
    }

    return (T) value;
  }
}
