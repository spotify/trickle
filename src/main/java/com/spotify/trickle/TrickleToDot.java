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
    if (!(graph instanceof TrickleGraph)) {
      writer.println("Unable to create dot from graph of type: " + graph.getClass());
      return;
    }

    ConnectedNode<?> connectedNode = ((TrickleGraph<?>) graph).getConnectedNode();

    writer.println("digraph TrickleGraph {");
    writeDependenciesForNode(connectedNode, writer);
    writer.println("}");
    writer.flush();
  }

  private static void writeDependenciesForNode(ConnectedNode<?> connectedNode,
                                               PrintWriter writer) {
    String safeNodeName = dotSafe(connectedNode.getName());
    writer.println(String.format("  %s [label=\"%s\"];", safeNodeName, connectedNode.getName()));

    int pos = 0;
    for (Object dep : connectedNode.getInputs()) {
      if (dep instanceof GraphDep) {
        ConnectedNode<?> from = ((GraphDep<?>) dep).getGraph().getConnectedNode();

        writer.println(String.format("  %s -> %s [label=\"arg%d\"];", dotSafe(from.getName()), safeNodeName, pos));
      }
      else {
        Name<?> name = ((BindingDep<?>) dep).getName();

        writer.println(String.format("  %s [label=\"%s\" shape=box];", dotSafe(name.getName()), name.getName()));
        writer.println(String.format("  %s -> %s [label=\"arg%d\"];", dotSafe(name.getName()), safeNodeName, pos));
      }

      pos++;
    }


    for (TrickleGraph<?> node : connectedNode.getPredecessors()) {
      ConnectedNode<?> from = node.getConnectedNode();

      writer.println(String.format("  %s -> %s [style=dotted];", dotSafe(from.getName()), safeNodeName));
    }
  }

  private static String dotSafe(String name) {
    return name.replaceAll(" ", "_");
  }
}
