package com.spotify.trickle.example;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.spotify.trickle.*;

import static com.spotify.trickle.Name.named;

/**
 * TODO: document!
 */
public class Heartbeats {
  public static final Name<Endpoint> ENDPOINT = named("endpoint", Endpoint.class);

  final long heartbeatIntervalMillis = 132;
  final Graph<Long> graph;

  public Heartbeats() {
    final Node1<Endpoint, RegistryEntry> fetchCurrent = arg -> queryEndpoints(arg);
    Node1<Endpoint, Boolean> updateState = Heartbeats::putEntry;
    Node1<RegistryEntry, Void> updateSerial = this::updateSerialNode;
    Node0<Long> returnResult = () -> Futures.immediateFuture(heartbeatIntervalMillis);

    graph = Trickle
        .graph(Long.class)
        .inputs(ENDPOINT)
        .call(fetchCurrent).with(ENDPOINT)
        .call(updateState).with(ENDPOINT).after(fetchCurrent)
        .call(updateSerial).with(fetchCurrent).after(updateState)
        .call(returnResult).after(updateSerial)
        .output(returnResult);
  }

  public ListenableFuture<Long> heartbeat(Endpoint endpoint) {
    return graph.bind(ENDPOINT, endpoint).run();
  }

  private ListenableFuture<Void> updateSerialNode(RegistryEntry entry) {
    if (entry == null || entry.getState() == State.DOWN) {
      return updateSerialNumber();
    } else {
      return Futures.immediateFuture(null);
    }
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
