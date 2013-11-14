package com.spotify.trickle;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/**
 * TODO: document!
 */
public class Java8Transformer<T> implements Transformer<T> {
  private final List<Dep<?>> inputs;
  private final Node.NodeMethod<?> method;

  public Java8Transformer(List<Dep<?>> inputs, Object obj) {
    this.inputs = inputs;
    this.method = (Node.NodeMethod<?>) obj;
  }

  @Override
  public ListenableFuture<T> createTransform(ImmutableList<Object> values, ListenableFuture<?> doneSignal) {
    return Futures.transform(doneSignal, (AsyncFunction<Object, T>) input -> (ListenableFuture<T>) method.run(values.toArray()));
  }
}
