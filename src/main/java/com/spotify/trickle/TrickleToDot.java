package com.spotify.trickle;

import java.io.PrintWriter;

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
    if (!(graph instanceof GraphBuilder)) {
      writer.println("Unable to create dot from graph of type: " + graph.getClass());
      return;
    }

    GraphBuilder<?> graphBuilder = (GraphBuilder<?>) graph;

    writer.println("digraph TrickleGraph {");
    writeDependenciesForNode(graphBuilder, writer);
    writer.println("}");
    writer.flush();
  }

  private static void writeDependenciesForNode(GraphBuilder<?> graphBuilder, PrintWriter writer) {
    String safeNodeName = dotSafe(graphBuilder.name);
    writer.println(String.format("  %s [label=\"%s\"];", safeNodeName, graphBuilder.name));

    int pos = 0;
    for (Object dep : graphBuilder.inputs) {
      if (dep instanceof GraphDep) {
        GraphBuilder<?> from = (GraphBuilder<?>) ((GraphDep<?>) dep).getGraph();

        writer.println(String.format("  %s -> %s [label=\"arg%d\"];", dotSafe(from.name),
                                     safeNodeName, pos));
      }
      else {
        Name<?> name = ((BindingDep<?>) dep).getName();

        writer.println(String.format("  %s [label=\"%s\" shape=box];", dotSafe(name.getName()), name.getName()));
        writer.println(String.format("  %s -> %s [label=\"arg%d\"];", dotSafe(name.getName()), safeNodeName, pos));
      }

      pos++;
    }


    for (Graph<?> node : graphBuilder.predecessors) {
      GraphBuilder<?> from = (GraphBuilder<?>) node;

      writer.println(String.format("  %s -> %s [style=dotted];", dotSafe(from.name),
                                   safeNodeName));
    }
  }

  private static String dotSafe(String name) {
    return name.replaceAll(" ", "_");
  }
}
