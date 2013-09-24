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
    PNode<RegistryEntry> fetchCurrentState = currentStateNode();
    PNode<Boolean> updateState = updateStateNode();
    PNode<Void> updateSerial = updateSerialNode();
    PNode<Long> returnResult = resultNode();

    graph = PTrickle
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

  private PNode<RegistryEntry> currentStateNode() {
    return PNode.of(new Object() {
      public ListenableFuture<RegistryEntry> _(Endpoint endpoint) {
        return queryEndpoints(endpoint);
      }
    });
  }

  private PNode<Boolean> updateStateNode() {
    return PNode.of(new Object() {
      public ListenableFuture<Boolean> _(Endpoint endpoint) {
        return putEntry(endpoint);
      }
    });
  }

  private PNode<Void> updateSerialNode() {
    return PNode.of(new Object() {
      public ListenableFuture<Void> _(RegistryEntry entry) {
        if (entry == null || entry.getState() == State.DOWN) {
          return updateSerialNumber();
        } else {
          return Futures.immediateFuture(null);
        }
      }
    });
  }

  private PNode<Long> resultNode() {
    return PNode.of(new Object() {
      public Long _() {
        return heartbeatIntervalMillis;
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
