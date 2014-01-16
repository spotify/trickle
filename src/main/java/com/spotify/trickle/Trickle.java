package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.spotify.trickle.TrickleNodeBuilder.NodeBuilder1;
import static com.spotify.trickle.TrickleNodeBuilder.NodeBuilder2;
import static com.spotify.trickle.TrickleNodeBuilder.NodeBuilder3;


/**
 * TODO: document!
 */
public final class Trickle {
  private Trickle() {
    // prevent instantiation
  }

  public static <R> NodeChainer<R> graph(Class<R> returnClass) {
    checkNotNull(returnClass, "returnClass");
    return new TrickleGraphBuilder<>();
  }

  public static <R> TrickleGraphBuilder<R> graph(TypeToken<R> returnClass) {
    checkNotNull(returnClass, "returnClass");
    return new TrickleGraphBuilder<>();
  }

  public static interface NeedsParameters1<A, N, R> {
    ConfigureOrChain<N, R> with(Value<A> arg1);
  }

  public static interface NeedsParameters2<A, B, N, R> {
    ConfigureOrChain<N, R> with(Value<A> arg1, Value<B> arg2);
  }

  public static interface NeedsParameters3<A, B, C, N, R> {
    ConfigureOrChain<N, R> with(Value<A> arg1, Value<B> arg2, Value<C> arg3);
  }

  public static interface FinalNeedsParameters1<A, R> {
    ConfigureOrBuild<R> with(Value<A> arg1);
  }

  public static interface FinalNeedsParameters2<A, B, R> {
    ConfigureOrBuild<R> with(Value<A> arg1, Value<B> arg2);
  }

  public static interface FinalNeedsParameters3<A, B, C, R> {
    ConfigureOrBuild<R> with(Value<A> arg1, Value<B> arg2, Value<C> arg3);
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
}
