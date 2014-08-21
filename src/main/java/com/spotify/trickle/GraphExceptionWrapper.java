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
 * TODO: document!
 */
public class GraphExceptionWrapper implements ExceptionWrapper {

  @Override
  public Throwable wrapException(Throwable t,
                                 TraverseState.FutureCallInformation currentCall,
                                 TraverseState traverseState) {
    return new GraphExecutionException(t, asCallInfo(currentCall), callInfos(traverseState));
  }

  private List<CallInfo> callInfos(TraverseState state) {
    ImmutableList.Builder<CallInfo> builder = builder();

    for (TraverseState.FutureCallInformation futureCallInformation : state.getCalls()) {
      if (futureCallInformation.isComplete()) {
        builder.add(asCallInfo(futureCallInformation));
      }
    }

    return builder.build();
  }

  private CallInfo asCallInfo(TraverseState.FutureCallInformation futureCallInformation) {
    List<ParameterValue<?>>
        parameterValues =
        asParameterValues(futureCallInformation.node.arguments(),
                          futureCallInformation.parameterFutures);

    return new CallInfo(futureCallInformation.node, parameterValues);
  }

  private List<ParameterValue<?>> asParameterValues(List<? extends NodeInfo> parameters,
                                                    List<ListenableFuture<?>> parameterFutures) {
    List<ParameterValue<?>> result = newLinkedList();

    for (int i = 0 ; i < parameters.size() ; i++) {
      result.add(new ParameterValue<Object>(parameters.get(i),
                                            inputValueFromFuture(parameterFutures.get(i))));
    }

    return result;
  }

  private Object inputValueFromFuture(ListenableFuture<?> input) {
    try {
      return Uninterruptibles.getUninterruptibly(input);
    } catch (ExecutionException e) {
      Throwables.propagateIfInstanceOf(e.getCause(), GraphExecutionException.class);
      throw Throwables.propagate(e);
    }
  }
}
