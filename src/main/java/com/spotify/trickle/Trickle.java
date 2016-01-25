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

import java.util.List;

/**
 * Static methods for constructing Trickle graphs. See the documentation at
 * <a href="https://github.com/spotify/trickle/wiki">the Trickle wiki</a> for more information.
 */
public final class Trickle {
  private Trickle() {
    // prevent instantiation
  }

  /**
   * Creates a graph consisting of a single node executing the supplied function.
   */
  public static <R> ConfigurableGraph<R> call(Func0<R> func) {
    return new GraphBuilder<R>(func);
  }

  /**
   * Initiates construction of a new sink node with a single parameter dependency, running the
   * supplied function.
   */
  public static <A, R> NeedsParameters1<A, R> call(Func1<A, R> func) {
    return new GraphBuilder.GraphBuilder1<A, R>(func);
  }

  /**
   * Initiates construction of a new sink node with two parameter dependencies, running the
   * supplied function.
   */
  public static <A, B, R> NeedsParameters2<A, B, R> call(Func2<A, B, R> func) {
    return new GraphBuilder.GraphBuilder2<A, B, R>(func);
  }

  /**
   * Initiates construction of a new sink node with three parameter dependencies, running the
   * supplied function.
   */
  public static <A, B, C, R> NeedsParameters3<A, B, C, R> call(Func3<A, B, C, R> func) {
    return new GraphBuilder.GraphBuilder3<A, B, C, R>(func);
  }

  /**
   * Initiates construction of a new sink node with four parameter dependencies, running the
   * supplied function.
   */
  public static <A, B, C, D, R> NeedsParameters4<A, B, C, D, R> call(Func4<A, B, C, D, R> func) {
    return new GraphBuilder.GraphBuilder4<A, B, C, D, R>(func);
  }

  /**
   * Initiates construction of a new sink node with five parameter dependencies, running the
   * supplied function.
   */
  public static <A, B, C, D, E, R> NeedsParameters5<A, B, C, D, E, R> call(Func5<A, B, C, D, E, R> func) {
    return new GraphBuilder.GraphBuilder5<A, B, C, D, E, R>(func);
  }

  /**
   * Initiates construction of a new sink node with five parameter dependencies, running the
   * supplied function.
   */
  public static <A, R> NeedsParameterList<A, R> call(ListFunc<A, R> func) {
    return new GraphBuilder.ListGraphBuilder<A, R>(func);
  }

  public interface NeedsParameters1<A, R> {
    /**
     * Indicate where to find values for the parameters required to invoke the function in this
     * node.
     */
    ConfigurableGraph<R> with(Parameter<A> arg1);
  }

  public interface NeedsParameters2<A, B, R> {
    /**
     * Indicate where to find values for the parameters required to invoke the function in this
     * node.
     */
    ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2);
  }

  public interface NeedsParameters3<A, B, C, R> {
    /**
     * Indicate where to find values for the parameters required to invoke the function in this
     * node.
     */
    ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2, Parameter<C> arg3);
  }

  public interface NeedsParameters4<A, B, C, D, R> {
    /**
     * Indicate where to find values for the parameters required to invoke the function in this
     * node.
     */
    ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2, Parameter<C> arg3, Parameter<D> arg4);
  }

  public interface NeedsParameters5<A, B, C, D, E, R> {
    /**
     * Indicate where to find values for the parameters required to invoke the function in this
     * node.
     */
    ConfigurableGraph<R> with(Parameter<A> arg1, Parameter<B> arg2, Parameter<C> arg3, Parameter<D> arg4, Parameter<E> arg5);
  }

  public interface NeedsParameterList<A, R> {
      ConfigurableGraph<R> with(List<? extends Parameter<A>> args);
  }
}
