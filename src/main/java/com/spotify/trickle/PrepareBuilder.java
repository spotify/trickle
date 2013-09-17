/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.collect.ImmutableMap.builder;

public class PrepareBuilder<T> {

  private final Node<T> node;

  private final ImmutableMap.Builder<Name, Object> bindingsBuilder = builder();

  public PrepareBuilder(Node<T> node) {
    this.node = node;
  }

  public <V> PrepareBuilder<T> bind(Name name, V value) {
    bindingsBuilder.put(name, value);
    return this;
  }

  public ListenableFuture<T> future() {
    return node.future(bindingsBuilder.build());
  }
}
