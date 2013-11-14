package com.spotify.trickle.example;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.spotify.trickle.*;

import static com.spotify.trickle.Name.named;

/**
 * TODO: document!
 */
public class Heartbeats {
  public static final Name ENDPOINT = named("endpoint");

  final long heartbeatIntervalMillis = 132;
  final Graph<Long> graph;

  public Heartbeats() {
    Node<RegistryEntry> fetchCurrentState = Node.of(args -> queryEndpoints((Endpoint) args[0]));
    Node<Boolean> updateState = Node.of(args -> putEntry((Endpoint) args[0]));
    Node<Void> updateSerial = updateSerialNode();
    Node<Long> returnResult = Node.of(args -> Futures.immediateFuture(heartbeatIntervalMillis));

    graph = Trickle
        .graph(Long.class)
        .inputs(ENDPOINT)
        .call(fetchCurrentState).with(ENDPOINT)
        .call(updateState).with(ENDPOINT).after(fetchCurrentState)
        .call(updateSerial).with(fetchCurrentState).after(updateState)
        .call(returnResult).after(updateSerial)
        .output(returnResult);
  }

  public ListenableFuture<Long> heartbeat(Endpoint endpoint) {
    return graph.bind(ENDPOINT, endpoint).run();
  }

  private Node<Void> updateSerialNode() {
    return Node.of(args -> {
      RegistryEntry entry = (RegistryEntry) args[0];

      if (entry == null || entry.getState() == State.DOWN) {
        return updateSerialNumber();
      } else {
        return Futures.immediateFuture(null);
      }
    });
  }


  private static ListenableFuture<Void> updateSerialNumber() {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private static ListenableFuture<Boolean> putEntry(Endpoint endpoint) {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private static ListenableFuture<RegistryEntry> queryEndpoints(Endpoint endpoint) {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  private static class RegistryEntry {
    private State state;

    public State getState() {
      return state;
    }

    public void setState(State state) {
      this.state = state;
    }
  }

  private static class Endpoint {
  }

  private static enum State {
    UP, DOWN
  }
}
