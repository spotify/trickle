package com.spotify.trickle;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration-level Trickle tests.
 */
public class TrickleApiTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldThrowForMultipleSinks() throws Exception {
    Node0<String> node1 = new Node0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return immediateFuture("one");
      }
    };
    Node0<String> node2 = new Node0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return immediateFuture("two");
      }
    };

    thrown.expect(TrickleException.class);
    thrown.expectMessage("ultiple sinks");
    thrown.expectMessage("the first sink");
    thrown.expectMessage("unnamed");

    Trickle.graph(String.class)
        .call(node1).named("the first sink")
        .finallyCall(node2)
        .build();
  }

  @Test
  public void shouldThrowForCycle() throws Exception {
    Node0<String> node1 = new Node0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return immediateFuture("1");
      }
    };
    Node1<String, String> node2 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String input) {
        return immediateFuture(input + "2");
      }
    };

    thrown.expect(TrickleException.class);
    thrown.expectMessage("cycle detected");
    thrown.expectMessage(either(containsString("node1 -> node2 -> node1")).or(containsString("node2 -> node1 -> node2")));

    Trickle.graph(String.class)
        .call(node1).after(node2).named("node1")
        .finallyCall(node2).with(node1).named("node2")
        .build();
  }

  @Test
  public void shouldThrowForComplexCycle() throws Exception {
    Node0<String> node1 = new Node0<String>() {
      @Override
      public ListenableFuture<String> run() {
        return immediateFuture("1");
      }
    };
    Node1<String, String> node2 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String input) {
        return immediateFuture(input + "2");
      }
    };
    Node1<String, String> node3 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String input) {
        return immediateFuture(input + "3");
      }
    };
    Node1<String, String> node4 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String input) {
        return immediateFuture(input + "4");
      }
    };

    thrown.expect(TrickleException.class);
    thrown.expectMessage("cycle detected");

    Trickle.graph(String.class)
        .call(node1).after(node3).named("node1")
        .call(node2).with(node1).named("node2")
        .call(node3).with(node2).named("node3")
        .finallyCall(node4).with(node2).named("node4")
        .build();
  }

//  @Test
//  public void shouldThrowForEmptyGraph() throws Exception {
//    thrown.expect(IllegalStateException.class);
//    thrown.expectMessage("Empty graph");
//
//    Trickle.graph(String.class)
//        .build();
//  }

  @Test
  public void shouldThrowForMissingInput() throws Exception {
    Node1<String, String> node1 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };

    Name<String> input = Name.named("somethingWeirdd", String.class);

    Graph<String> g = Trickle.graph(String.class)
        .finallyCall(node1).with(input)
        .build();

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name not bound to a value");
    thrown.expectMessage("somethingWeirdd");

    g.run();
  }

  @Test
  public void shouldThrowForDuplicateBindOfName() throws Exception {
    Node1<String, String> node1 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };

    Name<String> input = Name.named("mein Name", String.class);

    Graph<String> g = Trickle.graph(String.class)
        .finallyCall(node1).with(input)
        .build();

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Duplicate binding for name");
    thrown.expectMessage("mein Name");

    g.bind(input, "erich").bind(input, "volker");
  }

  @Test
  public void shouldWorkWithTypeToken() throws Exception {
    Node1<String, List<String>> node1 = new Node1<String, List<String>>() {
      @Override
      public ListenableFuture<List<String>> run(String arg) {
        List<String> testList = new ArrayList<>();
        testList.add(arg);
        return immediateFuture(testList);
      }
    };

    Name<String> input = Name.named("listValue1", String.class);

    Graph<List<String>> g = Trickle.graph(new TypeToken<List<String>>() {})
        .finallyCall(node1).with(input)
        .build();

    List<String> output = g.bind(input, "list value 1").run().get();
    assertThat(output.size(), is(1));
    assertThat(output.get(0), is("list value 1"));
  }

}
