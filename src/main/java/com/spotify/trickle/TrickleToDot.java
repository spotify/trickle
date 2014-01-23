package com.spotify.trickle;

import com.google.common.collect.Sets;

import java.io.PrintWriter;
import java.util.Set;

/**
 * Provides a method to write a graph to the DOT language, which allows it to be displayed
 * graphically for troubleshooting.
 *
 * TODO: document more/better!
 */
public final class TrickleToDot {
  private TrickleToDot() {
    // prevent instantiation
  }

  public static void writeToDot(Graph<?> graph, PrintWriter writer) {
    writer.println("digraph TrickleGraph {");
    writeDependenciesForNode(graph, writer, Sets.<GraphElement>newHashSet());
    writer.println("}");
    writer.flush();
  }

  private static void writeDependenciesForNode(Graph<?> graph, PrintWriter writer, Set<GraphElement> visited) {
    if (visited.contains(graph)) {
      return;
    }

    visited.add(graph);

    String safeNodeName = dotSafe(graph.name());
    writer.println(String.format("  %s [label=\"%s\"];", safeNodeName, graph.name()));

    int pos = 0;
    for (GraphElement dep : graph.inputs()) {
      if (dep.type() == GraphElement.Type.GRAPH) {
        writer.println(String.format("  %s -> %s [label=\"arg%d\"];", dotSafe(dep.name()),
                                     safeNodeName, pos));

        Graph<?> upstream = ((GraphDep<?>) dep).getGraph();

        writeDependenciesForNode(upstream, writer, visited);
      }
      else if (!visited.contains(dep)) {
        writer.println(String.format("  %s [label=\"%s\" shape=box];", dotSafe(dep.name()), dep.name()));
        writer.println(String.format("  %s -> %s [label=\"arg%d\"];", dotSafe(dep.name()), safeNodeName, pos));

        visited.add(dep);
      }

      pos++;
    }


    for (GraphElement node : graph.predecessors()) {
      Graph<?> from = (Graph<?>) node;

      writer.println(String.format("  %s -> %s [style=dotted];", dotSafe(from.name()),
                                   safeNodeName));

      writeDependenciesForNode(from, writer, visited);
    }
  }

  private static String dotSafe(String name) {
    return name.replaceAll(" ", "_");
  }
}
