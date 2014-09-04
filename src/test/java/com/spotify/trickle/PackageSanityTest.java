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

import com.google.common.base.Predicates;
import com.google.common.testing.AbstractPackageSanityTests;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import org.junit.Before;

import java.util.Collections;
import java.util.List;

public class PackageSanityTest extends AbstractPackageSanityTests {

  static final TraverseState.FutureCallInformation
      NO_INFO = new TraverseState.FutureCallInformation(
      new NodeInfo() {
        @Override
        public String name() {
          throw new UnsupportedOperationException();
        }

        @Override
        public List<? extends NodeInfo> arguments() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Iterable<? extends NodeInfo> predecessors() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Type type() {
          throw new UnsupportedOperationException();
        }
      },
      Collections.<ListenableFuture<?>>emptyList()
  );

  @Before
  @Override
  public void setUp() throws Exception {
    setDefault(Input.class, Input.named("hi"));
    // this is needed since otherwise, the wrong exception gets thrown by the ConnectedNode
    // constructor - no raw Nodes should ever be used, only NodeN:s, and it seems the
    // AbstractPackageSanityTests creates some non-null instance of Func to use
    final Func0<Object> node0 = new Func0<Object>() {
      @Override
      public ListenableFuture<Object> run() {
        throw new UnsupportedOperationException();
      }
    };
    setDefault(Func.class, node0);
    setDefault(TrickleNode.class, new TrickleNode() {
      @Override
      public ListenableFuture run(List values) {
        throw new UnsupportedOperationException();
      }
    });
    final GraphBuilder<?> graphBuilder = new GraphBuilder<Object>(node0);
    final NodeInfo nodeInfo = new FakeNodeInfo("hi",
                                                                         Collections.<NodeInfo>emptyList());
    final NodeInfo nodeInfo2 = new FakeNodeInfo("hey",
                                                                         Collections.<NodeInfo>emptyList());

    setDefault(Graph.class, graphBuilder);
    setDefault(GraphBuilder.class, graphBuilder);
    setDefault(TraverseState.class, TraverseState.empty(MoreExecutors.sameThreadExecutor(), false));
    setDefault(TraverseState.FutureCallInformation.class, NO_INFO);
    setDefault(CallInfo.class, new CallInfo(graphBuilder, Collections.<ParameterValue<?>>emptyList()));
    setDistinctValues(ParameterValue.class, new ParameterValue<Object>(nodeInfo, null), new ParameterValue<Object>(nodeInfo2, null));

    // test classes we don't need to worry about
    ignoreClasses(Predicates.<Class<?>>equalTo(Util.class));
    ignoreClasses(Predicates.<Class<?>>equalTo(FakeNodeInfo.class));

    super.setUp();
  }
}
