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

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.Trickle.call;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TrickleIntrospectionTest {
  Input<String> input;
  Func0<String> func0;
  Func1<String, String> func1;

  @Before
  public void setUp() throws Exception {
    input = Input.named("hi");

    func0 = new Func0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return immediateFuture("hey there");
      }
    };
    func1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg);
      }
    };
  }

  @Test
  public void shouldReturnEqualNodeInfosForSameNodeInTwoSubgraphs() throws Exception {
    Graph<String> root = call(func1).with(input);
    Graph<String> g1 = call(func1).with(root);
    Graph<String> g2 = call(func1).with(root);

    NodeInfo info1 = g1.arguments().iterator().next();
    NodeInfo info2 = g2.arguments().iterator().next();

    assertThat(info1, equalTo(info2));
  }

  @Test
  public void shouldReturnEqualNodeInfosForSameNodeBoundAndNotBound() throws Exception {
    Graph<String> root = call(func1).with(input);
    Graph<String> g1 = call(func1).with(root);
    Graph<String> g2 = g1.bind(input, "input value");

    NodeInfo info1 = g1.arguments().iterator().next();
    NodeInfo info2 = g2.arguments().iterator().next();

    assertThat(info1, equalTo(info2));
  }

  @Test
  public void shouldReturnEqualNodeInfosForSameNodeInTwoSubgraphsPredecessors() throws Exception {
    Graph<String> root = call(func1).with(input);
    Graph<String> g1 = call(func0).after(root);
    Graph<String> g2 = call(func0).after(root);

    NodeInfo info1 = g1.predecessors().iterator().next();
    NodeInfo info2 = g2.predecessors().iterator().next();

    assertThat(info1, equalTo(info2));
  }

  @Test
  public void shouldReturnEqualNodeInfosForSameNodeBoundAndNotBoundPredecessors() throws Exception {
    Graph<String> root = call(func1).with(input);
    Graph<String> g1 = call(func0).after(root);
    Graph<String> g2 = g1.bind(input, "input value");

    NodeInfo info1 = g1.predecessors().iterator().next();
    NodeInfo info2 = g2.predecessors().iterator().next();

    assertThat(info1, equalTo(info2));
  }

  @Test
  public void shouldReturnEqualNodeInfosForSameInputInTwoNodes() throws Exception {
    Graph<String> g1 = call(func1).with(input);
    Graph<String> g2 = call(func1).with(input);

    NodeInfo info1 = g1.arguments().iterator().next();
    NodeInfo info2 = g2.arguments().iterator().next();

    assertThat(info1, equalTo(info2));
  }


}
