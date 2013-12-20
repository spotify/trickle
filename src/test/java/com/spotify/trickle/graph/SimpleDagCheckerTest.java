package com.spotify.trickle.graph;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SimpleDagCheckerTest {
  SimpleDagChecker checker;

  @Before
  public void setUp() throws Exception {
    checker = new SimpleDagChecker();
  }

  @Test
  public void shouldHandleSingleNode() throws Exception {
    DagNode<String> node = new DagNode<>("hi", ImmutableSet.<DagNode<String>>of());
    Set<DagNode<String>> nodes = ImmutableSet.of(node);

    assertThat(checker.findSinks(nodes), equalTo(nodes));
  }

  @Test
  public void shouldFindSinkInDiamondCase() throws Exception {
    DagNode<String> sink = new DagNode<>("Hi", ImmutableSet.<DagNode<String>>of());
    DagNode<String> left = new DagNode<>("ho", ImmutableSet.of(sink));
    DagNode<String> right = new DagNode<>("hey", ImmutableSet.of(sink));
    DagNode<String> top = new DagNode<>("start", ImmutableSet.of(left, right));

    Set<DagNode<String>> expected = ImmutableSet.of(sink);

    assertThat(checker.findSinks(ImmutableSet.of(top, left, sink, right)), equalTo(expected));
  }

  @Test
  public void shouldFindSinksWhenMultiple() throws Exception {
    DagNode<String> left = new DagNode<>("ho", ImmutableSet.<DagNode<String>>of());
    DagNode<String> right = new DagNode<>("hey", ImmutableSet.<DagNode<String>>of());
    DagNode<String> top = new DagNode<>("start", ImmutableSet.of(left, right));

    Set<DagNode<String>> expected = ImmutableSet.of(left, right);

    assertThat(checker.findSinks(ImmutableSet.of(top, left, right)), equalTo(expected));
  }

  @Test
  public void shouldFindSinksWhenDisconnected() throws Exception {
    DagNode<String> left = new DagNode<>("ho", ImmutableSet.<DagNode<String>>of());
    DagNode<String> right = new DagNode<>("hey", ImmutableSet.<DagNode<String>>of());
    DagNode<String> top = new DagNode<>("start", ImmutableSet.of(left));

    Set<DagNode<String>> expected = ImmutableSet.of(left, right);

    assertThat(checker.findSinks(ImmutableSet.of(top, left, right)), equalTo(expected));
  }
}
