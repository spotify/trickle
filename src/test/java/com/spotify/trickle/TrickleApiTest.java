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
    Node1<String, String> node1 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        return immediateFuture(arg + ", 1");
      }
    };

    Name<String> input = Name.named("somethingWeirdd", String.class);

    Graph<String> g = call(node1).with(input);

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

    Graph<String> g = call(node1).with(input);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Duplicate binding for name");
    thrown.expectMessage("mein Name");

    g.bind(input, "erich").bind(input, "volker");
  }

}
