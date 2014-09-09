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

import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.spotify.trickle.GraphExceptionWrapper.wrapException;

/**
 * Fallback that handles errors when executing a graph node.
 */
class NodeExecutionFallback<R> implements FutureFallback<R> {

  private final TraverseState.FutureCallInformation currentCall;
  private final TraverseState state;
  private final GraphBuilder<R> graph;

  public NodeExecutionFallback(GraphBuilder<R> graph,
                               TraverseState.FutureCallInformation currentCall,
                               TraverseState state) {
    this.currentCall = checkNotNull(currentCall);
    this.state = checkNotNull(state);
    this.graph = checkNotNull(graph);
  }

  @Override
  public ListenableFuture<R> create(Throwable t) {
    if (graph.getFallback().isPresent()) {
      try {
        return graph.getFallback().get().apply(t);
      } catch (Exception e) {
        return immediateFailedFuture(wrapIfNeeded(e));
      }
    }

    return immediateFailedFuture(wrapIfNeeded(t));
  }

  private Throwable wrapIfNeeded(Throwable t) {
    if (t instanceof GraphExecutionException) {
      return t;
    }

    return wrapException(t, currentCall, state);
  }
}
