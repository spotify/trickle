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
  private final TNode<?> method;

  public Java8Transformer(List<Dep<?>> inputs, TNode<?> obj) {
    this.inputs = inputs;
    this.method = obj;
  }

  @Override
  public ListenableFuture<T> createTransform(ImmutableList<Object> values, ListenableFuture<?> doneSignal) {
    switch (values.size()) {
      case 0:
        return Futures.transform(doneSignal, (AsyncFunction<Object, T>) input -> ((Node0<T>) method).run());
      case 1:
        return Futures.transform(doneSignal, (AsyncFunction<Object, T>) input -> ((Node1<Object, T>) method).run(values.get(0)));
      case 2:
        return Futures.transform(doneSignal, (AsyncFunction<Object, T>) input -> ((Node2<Object, Object, T>) method).run(values.get(0), values.get(1)));
      case 3:
        return Futures.transform(doneSignal, (AsyncFunction<Object, T>) input -> ((Node3<Object, Object, Object, T>) method).run(values.get(0), values.get(1), values.get(2)));
      default:
        throw new UnsupportedOperationException("bleh");
    }
  }
}
