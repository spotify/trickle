/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Node<T> {
  private static final AtomicInteger c = new AtomicInteger();

  final Class<T> returnCls;
  final int n;

  public Node(Class<T> returnCls) {
    this.returnCls = returnCls;

    n = c.getAndIncrement();
  }

  public PrepareBuilder<T> prepare() {
    return new PrepareBuilder<>(this);
  }

  ListenableFuture<T> future(ImmutableMap<Name, Object> bindings) {
    return future(bindings, new HashMap<Node<?>, ListenableFuture<?>>());
  }

  protected abstract ListenableFuture<T> future(
      ImmutableMap<Name, Object> bindings,
      Map<Node<?>, ListenableFuture<?>> visited);

  public void printTree() {
    printTree(System.out);
  }

  public void printTree(PrintStream out) {
    printTree(out, new Stack<Boolean>());
  }

  void printTree(PrintStream out, Stack<Boolean> indent) {
    final String ind = indentStr(indent);
    out.println(ind + this + " (" + returnCls + ")");
    printInfo(out, indent);
  }

  private static final String LINEINDENT  = "  |  ";
  private static final String BLANKINDENT = "     ";
  final Joiner joiner = Joiner.on("");

  protected String indentStr(final Stack<Boolean> indent) {
    if (indent.isEmpty()) {
      return "";
    }

    final Iterable<String> string = Iterables.transform(indent, new Function<Boolean, String>() {
      @Override
      public String apply(Boolean input) {
        return input ? LINEINDENT : BLANKINDENT;
      }
    });

    final Iterable<String> indentSegments = Iterables.limit(string, indent.size() - 1);

    final String indentString = joiner.join(indentSegments);
    return indentString + (indent.peek() ? "  |-> " : "  `-> ");
  }

  protected abstract void printInfo(PrintStream out, Stack<Boolean> indent);

  @Override
  public String toString() {
    return "Node{" + n + '}';
  }
}
