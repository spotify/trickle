package com.spotify.trickle;

import java.util.List;

/**
 * TODO: document!
 */
public interface GraphElement {
  String name();
  List<? extends GraphElement> inputs();
  List<? extends GraphElement> predecessors();
  Type type();

  public static enum Type {
    CONSTANT,
    GRAPH
  }
}
