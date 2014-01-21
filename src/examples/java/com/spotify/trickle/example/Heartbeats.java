package com.spotify.trickle.example;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.spotify.trickle.*;

import static com.spotify.trickle.Name.named;
import static com.spotify.trickle.Trickle.call;

/**
 * TODO: document!
 */
public class Heartbeats {
  public static final Name<Endpoint> ENDPOINT = named("endpoint", Endpoint.class);

  final long heartbeatIntervalMillis = 132;
  final Graph<Long> graph;

  public Heartbeats() {
    final Node1<Endpoint, RegistryEntry> fetchCurrent = fetchCurrent();
    Node1<Endpoint, Boolean> updateState = updateState();
    Node1<RegistryEntry, Void> updateSerial = updateSerial();
    Node0<Long> returnResult = returnHeartbeatInterval();

    Graph<RegistryEntry> g1 = call(fetchCurrent).with(ENDPOINT);
    Graph<Boolean> g2       = call(updateState).with(ENDPOINT).after(g1);
    Graph<Void> g3          = call(updateSerial).with(g1).after(g2);
    graph = call(returnResult).after(g3);
  }

  private Node1<Endpoint, RegistryEntry> fetchCurrent() {
    return new Node1<Endpoint, RegistryEntry>() {
      @Override
      public ListenableFuture<RegistryEntry> run(Endpoint arg) {
        return null;
      }
    };
  }

  private Node1<Endpoint, Boolean> updateState() {
    return new Node1<Endpoint, Boolean>() {
      @Override
      public ListenableFuture<Boolean> run(Endpoint endpoint) {
        return null;
      }
    };
  }

  private Node1<RegistryEntry, Void> updateSerial() {
    return new Node1<RegistryEntry, Void>() {
      @Override
      public ListenableFuture<Void> run(RegistryEntry arg) {
        if (arg == null || arg.getState() == State.DOWN) {
          return updateSerialNumber();
        } else {
          return Futures.immediateFuture(null);
        }
      }
    };
  }

  private Node0<Long> returnHeartbeatInterval() {
    return new Node0<Long>() {
      @Override
      public ListenableFuture<Long> run() {
        return Futures.immediateFuture(heartbeatIntervalMillis);
      }
    };
  }

  public ListenableFuture<Long> heartbeat(Endpoint endpoint) {
    return graph.bind(ENDPOINT, endpoint).run(MoreExecutors.sameThreadExecutor());
  }


  private static ListenableFuture<Void> updateSerialNumber() {
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
