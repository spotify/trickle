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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.GraphExceptionWrapper.wrapException;
import static com.spotify.trickle.Util.hasAncestor;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class GraphExceptionWrapperTest {

  private static final List<NodeInfo> NO_ARGS = Collections.emptyList();
  private static final List<ListenableFuture<?>> NO_VALUES = emptyList();

  Throwable t;
  TraverseState traverseState;
  TraverseState.FutureCallInformation currentCall;

  NodeInfo currentNodeInfo;
  List<ListenableFuture<?>> currentNodeValues;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    t = new RuntimeException("the original problem");

    Map<Input<?>, Object> emptyMap = Collections.emptyMap();
    traverseState = new TraverseState(emptyMap, MoreExecutors.sameThreadExecutor(), true);

    List<? extends NodeInfo> currentNodeParameters = ImmutableList.of(
        new FakeNodeInfo("arg1", Collections .<NodeInfo>emptyList()),
        new FakeNodeInfo("argument 2", Collections .<NodeInfo>emptyList())
    );

    currentNodeInfo = new FakeNodeInfo("the node", currentNodeParameters);
    currentNodeValues = ImmutableList.<ListenableFuture<?>>of(
        immediateFuture("value 1"),
        immediateFuture("andra värdet")
    );
    currentCall = new TraverseState.FutureCallInformation(currentNodeInfo, currentNodeValues);
  }

  @Test
  public void shouldHaveOriginalExceptionAsCause() throws Exception {
    assertThat(wrapException(t, currentCall, traverseState).getCause(), equalTo(t));
  }

  @Test
  public void shouldIncludeCurrentNodeInMessage() throws Exception {
    String message = wrapException(t, currentCall, traverseState).getMessage();

    assertThat(message, containsString(currentNodeInfo.name()));
  }

  @Test
  public void shouldIncludeCurrentNodeParametersInMessage() throws Exception {
    String message = wrapException(t, currentCall, traverseState).getMessage();

    for (NodeInfo parameter : currentNodeInfo.arguments()) {
      assertThat(message, containsString(parameter.name()));
    }
  }

  @Test
  public void shouldIncludeCurrentNodeValuesInMessage() throws Exception {
    String message = wrapException(t, currentCall, traverseState).getMessage();

    for (ListenableFuture<?> value : currentNodeValues) {
      assertThat(message, containsString(value.get().toString()));
    }
  }

  @Test
  public void shouldIncludeCompletedCallsInInfo() throws Exception {
    FakeNodeInfo node1 = new FakeNodeInfo("completed 1", NO_ARGS);
    FakeNodeInfo node2 = new FakeNodeInfo("completed 2",
                                          ImmutableList.<NodeInfo>of(
                                              new FakeNodeInfo("param 1", NO_ARGS),
                                              new FakeNodeInfo("param 2", NO_ARGS)
                                          ));
    traverseState.record(node1, NO_VALUES);
    traverseState.record(node2, asFutures("value 1", "value 2"));

    GraphExecutionException e =
        (GraphExecutionException) wrapException(t, currentCall, traverseState);

    assertThat(e.getCalls().size(), equalTo(2));

    boolean found1 = false;
    boolean found2 = false;

    for (CallInfo callInfo : e.getCalls()) {
      if (callInfo.getNodeInfo().equals(node1)) found1 = true;
      if (callInfo.getNodeInfo().equals(node2)) found2 = true;
    }

    assertThat(found1, is(true));
    assertThat(found2, is(true));
  }

  @Test
  public void shouldNotIncludeIncompleteCallsInInfo() throws Exception {
    FakeNodeInfo node1 = new FakeNodeInfo("completed 1", NO_ARGS);
    FakeNodeInfo node2 = new FakeNodeInfo("incomplete 2",
                                          ImmutableList.<NodeInfo>of(
                                              new FakeNodeInfo("param 1", NO_ARGS)
                                          ));
    SettableFuture<?> future = SettableFuture.create();

    traverseState.record(node1, NO_VALUES);
    traverseState.record(node2, ImmutableList.<ListenableFuture<?>>of(future));

    GraphExecutionException e =
        (GraphExecutionException) wrapException(t, currentCall, traverseState);

    assertThat(e.getCalls().size(), equalTo(1));

    boolean found1 = false;
    boolean found2 = false;

    for (CallInfo callInfo : e.getCalls()) {
      if (callInfo.getNodeInfo().equals(node1)) found1 = true;
      if (callInfo.getNodeInfo().equals(node2)) found2 = true;
    }

    assertThat(found1, is(true));
    assertThat(found2, is(false));
  }

  @Test
  public void shouldReportIncompleteInputs() throws Exception {
    ListenableFuture<Object> element = SettableFuture.create();
    List<ListenableFuture<?>> parameterValues = ImmutableList.of(
        immediateFuture("hi"),
        element
    );

    currentCall = new TraverseState.FutureCallInformation(currentNodeInfo, parameterValues);

    String message = wrapException(t, currentCall, traverseState).getMessage();

    assertThat(message, containsString("NOT TERMINATED FUTURE"));
  }

  @Test
  public void shouldThrowForFailedInputs() throws Exception {
    RuntimeException inputException = new RuntimeException("failing input");
    List<ListenableFuture<?>> parameterValues = ImmutableList.of(
        immediateFailedFuture(inputException),
        immediateFuture("hi")
    );

    currentCall = new TraverseState.FutureCallInformation(currentNodeInfo, parameterValues);

    thrown.expect(hasAncestor(inputException));

    wrapException(t, currentCall, traverseState).getMessage();
 }

  @Test
  public void shouldRetainGraphExecutionExceptions() throws Exception {
    CallInfo currentCallInfo = new CallInfo(currentNodeInfo, ImmutableList.<ParameterValue<?>>of());
    List<CallInfo> previousCalls = ImmutableList.of();

    Exception inputException = new GraphExecutionException(null, currentCallInfo, previousCalls);

    List<ListenableFuture<?>> parameterValues = ImmutableList.of(
        immediateFailedFuture(inputException),
        immediateFuture("hi")
    );

    currentCall = new TraverseState.FutureCallInformation(currentNodeInfo, parameterValues);

    thrown.expect(is(inputException));

    wrapException(t, currentCall, traverseState).getMessage();
 }

  private List<ListenableFuture<?>> asFutures(String... values) {
    return Lists.transform(Arrays.asList(values), new Function<String, ListenableFuture<?>>() {
      @Nullable
      @Override
      public ListenableFuture<?> apply(@Nullable String input) {
        return immediateFuture(input);
      }
    });
  }
}