package com.spotify.trickle;

/**
 * Defines operations possible on an intermediate node builder when constructing a graph.
 */
public interface NodeChainer<R> {
  <O> ConfigureOrChain<O, R> call(Node0<O> node);

  <A, O> Trickle.NeedsParameters1<A, O, R> call(Node1<A, O> node);

  <A, B, O> Trickle.NeedsParameters2<A, B, O, R> call(Node2<A, B, O> node);

  <A, B, C, O> Trickle.NeedsParameters3<A, B, C, O, R> call(Node3<A, B, C, O> node);

  ConfigureOrBuild<R> finallyCall(Node0<R> node);

  <A> Trickle.FinalNeedsParameters1<A, R> finallyCall(Node1<A, R> node);

  <A, B> Trickle.FinalNeedsParameters2<A, B, R> finallyCall(Node2<A, B, R> node);

  <A, B, C> Trickle.FinalNeedsParameters3<A, B, C, R> finallyCall(Node3<A, B, C, R> node);
}
