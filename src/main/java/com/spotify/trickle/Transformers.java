/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import java.util.List;

final class Transformers {
  private Transformers() {}

  public static Transformer<?> newNoChecksTransformer(
      final List<Dep<?>> inputs,
      final Node<?> obj) {
    return new Java8Transformer<>(inputs, obj);
  }
}
