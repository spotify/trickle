/*
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.trickle;

/**
 * TODO: document! This class documentation should have some examples, rationale, etc.
 */
public final class Trickle {
  private Trickle() {
    // prevent instantiation
  }

  public static <R> ConfigurableGraph<R> call(Func0<R> node) {
    return new GraphBuilder<R>(node);
  }

  public static <A, R> NeedsParameters1<A, R> call(Func1<A, R> node) {
    return new GraphBuilder.GraphBuilder1<A, R>(node);
  }

  public static <A, B, R> NeedsParameters2<A, B, R> call(Func2<A, B, R> node) {
    return new GraphBuilder.GraphBuilder2<A, B, R>(node);
  }

  public static <A, B, C, R> NeedsParameters3<A, B, C, R> call(Func3<A, B, C, R> node) {
    return new GraphBuilder.GraphBuilder3<A, B, C, R>(node);
  }

  public interface NeedsParameters1<A, R> {
    ConfigurableGraph<R> with(Parameter<A> arg1);
  }

  public interface NeedsParameters2<A, B, R> {
    ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2);
  }

  public interface NeedsParameters3<A, B, C, R> {
    ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2, Parameter<C> arg3);
  }

}
