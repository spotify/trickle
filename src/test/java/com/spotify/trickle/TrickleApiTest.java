package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.Trickle.call;

/**
 * Integration-level Trickle tests.
 */
public class TrickleApiTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldThrowForMissingInput() throws Exception {
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };

    Name<String> input = Name.named("somethingWeirdd");

    Graph<String> g = call(node1).with(input);

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Name not bound to a value");
    thrown.expectMessage("somethingWeirdd");

    g.run();
  }

  @Test
  public void shouldThrowForDuplicateBindOfName() throws Exception {
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };

    Name<String> input = Name.named("mein Name");

    Graph<String> g = call(node1).with(input);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Duplicate binding for name");
    thrown.expectMessage("mein Name");

    g.bind(input, "erich").bind(input, "volker");
  }

  @Test
  public void shouldThrowForDuplicateBindOfNameInChainedSubgraphs() throws Exception {
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };

    Name<String> input = Name.named("mein Name");

    Graph<String> g1 = call(node1).with(input).bind(input, "erich");
    Graph<String> g2 = call(node1).with(g1);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Duplicate binding for name");
    thrown.expectMessage("mein Name");

    g2.bind(input, "volker").run();
  }

  @Test
  public void shouldThrowForDuplicateBindOfNameInDiamondSubgraphs() throws Exception {
    Func1<String, String> node1 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };
   Func2<String, String, String> node2 = new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg, String arg2) {
        return immediateFuture(arg + ", " + arg2);
      }
    };

    Name<String> input = Name.named("mitt namn");
    Name<String> input2 = Name.named("nåt");

    Graph<String> g1 = call(node1).with(input).bind(input, "erik").bind(input2, "hej");
    Graph<String> g2 = call(node1).with(input).bind(input, "folke").bind(input2, "hopp");

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Duplicate binding for name");
    thrown.expectMessage("mitt namn");
    thrown.expectMessage("nåt");

    // creating the 'bad' graph after setting up the thrown expectations, since it would be nice
    // to be able to detect the problem at construction time rather than runtime.
    Graph<String> g3 = call(node2).with(g1, g2);
    g3.run();
  }
}
