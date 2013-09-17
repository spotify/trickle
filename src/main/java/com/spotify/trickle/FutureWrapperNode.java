/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.PrintStream;
import java.util.Map;
import java.util.Stack;

class FutureWrapperNode<T> extends Node<T> {

  private final ListenableFuture<T> future;

  FutureWrapperNode(
      final Class<T> returnCls,
      final ListenableFuture<T> future) {
    super(returnCls);
    this.future = future;
  }

  @Override
  protected ListenableFuture<T> future(
      final ImmutableMap<Name, Object> bindings,
      final Map<Node<?>, ListenableFuture<?>> visited) {
    return future;
  }

  @Override
  protected void printInfo(PrintStream out, Stack<Boolean> indent) {
    out.println("Future Node " + future);
  }

}
