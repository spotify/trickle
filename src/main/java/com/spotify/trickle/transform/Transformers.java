/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle.transform;

import com.spotify.trickle.Trickle;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class Transformers {
  private Transformers() {}

  public static <T> Transformer<T> newMethodTransformer(
      final ImmutableList<Trickle.Dep<?>> inputs,
      final Class<T> returnCls,
      final Object obj) {
    return new MethodTransformer<>(inputs, returnCls, obj);
  }

  public static Transformer<?> newNoChecksTransformer(
      final List<Trickle.Dep<?>> inputs,
      final Object obj) {
    return new NoChecksMethodTransformer<>(inputs, obj);
  }

  public static <T> Transformer<T> newDirectTransformer() {
    return new DirectTransformer<>();
  }
}
