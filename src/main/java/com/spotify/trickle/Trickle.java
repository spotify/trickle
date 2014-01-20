package com.spotify.trickle;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * TODO: document! This class documentation should have some examples, rationale, etc.
 */
public final class Trickle {
  private Trickle() {
    // prevent instantiation
  }

  public static <R> NodeChainer<R> graph(Class<R> returnClass) {
    checkNotNull(returnClass, "returnClass");
    return new TrickleGraphBuilder<R>();
  }

  public static <R> TrickleGraphBuilder<R> graph(TypeToken<R> returnClass) {
    checkNotNull(returnClass, "returnClass");
    return new TrickleGraphBuilder<R>();
  }

  public interface NeedsParameters1<A, N, R> {
    ConfigureOrChain<N, R> with(Value<A> arg1);
  }

  public interface NeedsParameters2<A, B, N, R> {
    ConfigureOrChain<N, R> with(Value<A> arg1, Value<B> arg2);
  }

  public interface NeedsParameters3<A, B, C, N, R> {
    ConfigureOrChain<N, R> with(Value<A> arg1, Value<B> arg2, Value<C> arg3);
  }

  public interface FinalNeedsParameters1<A, R> {
    ConfigureOrBuild<R> with(Value<A> arg1);
  }

  public interface FinalNeedsParameters2<A, B, R> {
    ConfigureOrBuild<R> with(Value<A> arg1, Value<B> arg2);
  }

  public interface FinalNeedsParameters3<A, B, C, R> {
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
