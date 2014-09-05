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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.ImmutableList.builder;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * Wraps exceptions that happen during graph execution, providing information meant to aid
 * troubleshooting.
 */
final class GraphExceptionWrapper {

  private GraphExceptionWrapper() {
    // prevent instantiation
  }

  public static Throwable wrapException(Throwable t,
                                        TraverseState.FutureCallInformation currentCall,
                                        TraverseState traverseState) {
    return new GraphExecutionException(t, asCallInfo(currentCall), callInfos(traverseState));
  }

  private static List<CallInfo> callInfos(TraverseState state) {
    ImmutableList.Builder<CallInfo> builder = builder();

    for (TraverseState.FutureCallInformation futureCallInformation : state.getCalls()) {
      if (futureCallInformation.isComplete()) {
        builder.add(asCallInfo(futureCallInformation));
      }
    }

    return builder.build();
  }

  private static CallInfo asCallInfo(TraverseState.FutureCallInformation futureCallInformation) {
    return new CallInfo(futureCallInformation.node,
                        asParameterValues(futureCallInformation.node.arguments(),
                                          futureCallInformation.parameterFutures));
  }

  private static List<ParameterValue<?>> asParameterValues(List<? extends NodeInfo> parameters,
                                                           List<ListenableFuture<?>> parameterFutures) {
    List<ParameterValue<?>> result = newLinkedList();

    for (int i = 0 ; i < parameters.size() ; i++) {
      result.add(new ParameterValue<Object>(parameters.get(i),
                                            inputValueFromFuture(parameterFutures.get(i))));
    }

    return result;
  }

  private static Object inputValueFromFuture(ListenableFuture<?> input) {
    try {
      return Uninterruptibles.getUninterruptibly(input);
    } catch (ExecutionException e) {
      Throwables.propagateIfInstanceOf(e.getCause(), GraphExecutionException.class);
      throw Throwables.propagate(e);
    }
  }
}
