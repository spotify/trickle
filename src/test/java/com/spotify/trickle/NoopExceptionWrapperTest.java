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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class NoopExceptionWrapperTest {
  NoopExceptionWrapper wrapper;
  TraverseState.FutureCallInformation currentCall;
  TraverseState traverseState;


  @Before
  public void setUp() throws Exception {
    wrapper = new NoopExceptionWrapper();

    currentCall = mock(TraverseState.FutureCallInformation.class);
    traverseState = mock(TraverseState.class);
  }

  @Test
  public void shouldReturnOriginalException() throws Exception {
    Throwable t = new RuntimeException("hi there");

    assertThat(wrapper.wrapException(t, currentCall, traverseState), equalTo(t));
  }
}