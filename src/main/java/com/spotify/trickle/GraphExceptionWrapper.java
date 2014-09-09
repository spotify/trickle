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

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

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
    return new CallInfo(futureCallInformation.getNode(),
                        asParameterValues(futureCallInformation.getNode().arguments(),
                                          futureCallInformation.getParameterFutures()));
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
    if (input.isDone()) {
      return Futures.getUnchecked(input);
    }
    else {
      return "NOT TERMINATED FUTURE";
    }
  }
}
