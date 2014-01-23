package com.spotify.trickle;

import com.google.common.base.Function;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * TODO: document! This class documentation should have some examples, rationale, etc.
 */
public final class Trickle {
  private Trickle() {
    // prevent instantiation
  }

  public static <R> ConfigurableGraph<R> call(Node0<R> node) {
    checkNotNull(node, "node");
    return new GraphBuilder<R>(node);
  }

  public static <A, R> NeedsParameters1<A, R> call(Node1<A, R> node) {
    checkNotNull(node, "node");
    return new GraphBuilder.GraphBuilder1<A, R>(node);
  }

  public static <A, B, R> NeedsParameters2<A, B, R> call(Node2<A, B, R> node) {
    checkNotNull(node, "node");
    return new GraphBuilder.GraphBuilder2<A, B, R>(node);
  }

  public static <A, B, C, R> NeedsParameters3<A, B, C, R> call(Node3<A, B, C, R> node) {
    checkNotNull(node, "node");
    return new GraphBuilder.GraphBuilder3<A, B, C, R>(node);
  }

  public interface NeedsParameters1<A, R> {
    ConfigurableGraph<R> with(Value<A> arg1);
  }

  public interface NeedsParameters2<A, B, R> {
    ConfigurableGraph<R> with(Value<A> arg1, Value<B> arg2);
  }

  public interface NeedsParameters3<A, B, C, R> {
    ConfigurableGraph<R> with(Value<A> arg1, Value<B> arg2, Value<C> arg3);
  }

  public static <T> Function<Throwable, T> always(@Nullable final T value) {
    return new Function<Throwable, T>() {
      @Nullable
      @Override
      public T apply(@Nullable Throwable input) {
        return value;
      }
    };
  }

//  public static run(Graph g, BoundValue... inputs) {}
//  public static BoundValue bind(Name n, Object value) {}
}
