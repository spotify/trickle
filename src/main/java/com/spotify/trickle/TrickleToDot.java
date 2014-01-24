package com.spotify.trickle;

import com.google.common.collect.Sets;

import java.io.PrintWriter;
import java.util.Set;

/**
 * Provides a method to write a graph to the DOT language, which allows it to be displayed
 * graphically for troubleshooting.
 *
 * TODO: this class is experimental, in order to prove a point. We'll revisit troubleshooting later.
 */
public final class TrickleToDot {
  private TrickleToDot() {
    // prevent instantiation
  }

  public static void writeToDot(Graph<?> graph, PrintWriter writer) {
    writer.println("digraph TrickleGraph {");
    writeDependenciesForNode(graph, writer, Sets.<NodeInfo>newHashSet());
    writer.println("}");
    writer.flush();
  }

  private static void writeDependenciesForNode(NodeInfo node, PrintWriter writer, Set<NodeInfo> visited) {
    if (visited.contains(node)) {
      return;
    }

    visited.add(node);

    String safeNodeName = dotSafe(node.name());

    writer.println(String.format("  %s [label=\"%s\"%s];", safeNodeName, node.name(), (node.type() == NodeInfo.Type.NODE ? "" : " shape=box")));

    int pos = 0;
    for (NodeInfo input : node.arguments()) {
      writeDependenciesForNode(input, writer, visited);

      writer.println(String.format("  %s -> %s [label=\"arg%d\"];", dotSafe(input.name()), safeNodeName, pos));

      pos++;
    }


    for (NodeInfo predecessor : node.predecessors()) {
      writeDependenciesForNode(predecessor, writer, visited);

      writer.println(String.format("  %s -> %s [style=dotted];", dotSafe(predecessor.name()), safeNodeName));
    }
  }

  private static String dotSafe(String name) {
    return name.replaceAll(" ", "_");
  }
}
