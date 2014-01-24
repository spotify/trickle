package com.spotify.trickle;

import java.util.List;

/**
 * TODO: document!
 */
public interface NodeInfo {
  String name();
  List<? extends NodeInfo> inputs();
  List<? extends NodeInfo> predecessors();
  Type type();

  public static enum Type {
    PARAMETER,
    GRAPH
  }
}
