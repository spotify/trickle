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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.Util.hasAncestor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeExecutionFallbackTest {
  NodeExecutionFallback<String> fallback;
  GraphBuilder<String> graphBuilder;
  TraverseState traverseState;
  TraverseState.FutureCallInformation currentCall;

  NodeInfo currentNodeInfo;

  @Before
  public void setUp() throws Exception {
    //noinspection unchecked
    graphBuilder = mock(GraphBuilder.class);
    when(graphBuilder.getFallback())
        .thenReturn(Optional.<AsyncFunction<Throwable, String>>absent());

    Map<Input<?>, Object> emptyMap = Collections.emptyMap();
    traverseState = new TraverseState(emptyMap, MoreExecutors.sameThreadExecutor(), true);

    List<? extends NodeInfo> currentNodeParameters = ImmutableList.of();

    currentNodeInfo = new FakeNodeInfo("the node", currentNodeParameters);
    List<ListenableFuture<?>> currentNodeValues = ImmutableList.of();

    currentCall = new TraverseState.FutureCallInformation(currentNodeInfo, currentNodeValues);

    fallback = new NodeExecutionFallback<String>(graphBuilder, currentCall, traverseState);
  }

  @Test
  public void shouldNotWrapGraphExecutionException() throws Exception {
    Throwable expected = new GraphExecutionException(null, new CallInfo(currentNodeInfo, ImmutableList.<ParameterValue<?>>of()), ImmutableList.<CallInfo>of());

    ListenableFuture<String> future = fallback.create(expected);

    try {
      future.get();
      fail("expected an exception");
    }
    catch (ExecutionException e) {
      assertThat(e.getCause(), equalTo(expected));
    }
  }

  @Test
  public void shouldWrapGeneralException() throws Exception {
    Throwable expected = new RuntimeException("expected");

    ListenableFuture<String> future = fallback.create(expected);

    try {
      future.get();
      fail("expected an exception");
    }
    catch (ExecutionException e) {
      assertThat(e.getCause(), not(equalTo(expected)));
      assertThat(e, hasAncestor(expected));
    }
  }
}