package com.spotify.trickle;

import java.util.List;

/**
 * Interface defining the debug information that is available about a Trickle node or input.
 */
public interface NodeInfo {
  /**
   * @return the node name - defaults to "unnamed"
   */
  String name();

  /**
   * Gets the ordered list of node arguments.
   *
   * @return the possibly empty list of node arguments.
   */
  List<? extends NodeInfo> arguments();

  /**
   * Gets the possibly empty, unordered collection of nodes that must be completed before this
   * one can be executed.
   *
   * @return the collection of predecessors
   */
  Iterable<? extends NodeInfo> predecessors();

  /**
   * Returns the node type:
   * <ul>
   *   <li>PARAMETER = this is a parameter to the graph as a whole.</li>
   *   <li>NODE = this is a node in the graph</li>
   * </ul>
   * @return the node type
   */
  Type type();

  public static enum Type {
    PARAMETER,
    NODE
  }
}
