package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * TODO: document!
 */
public class TrickleToDotTest {

  @Test
  public void shouldGenerateDot() throws Exception {
    Node0<String> node0 = new Node0<String>() {
      @Override
      public ListenableFuture<String> run() {
        throw new UnsupportedOperationException();
      }
    };
    Node2<String, String, String> node1 = new Node2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg, String arg2) {
        throw new UnsupportedOperationException();
      }
    };
    Node3<String, String, String, String> node2 = new Node3<String, String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg, String arg2, String arg3) {
        throw new UnsupportedOperationException();
      }
    };
    Node1<String, String> node3 = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        throw new UnsupportedOperationException();
      }
    };

    Name<String> first = Name.named("first input", String.class);
    Name<String> second = Name.named("second input", String.class);

    Graph<String> g = Trickle.graph(String.class)
        .call(node0).named("node 0")
        .call(node1).with(node0, first).named("node 1")
        .call(node2).with(node1, node0, second).named("node 2")
        .call(node3).with(second).after(node1, node2)
        .build();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(outputStream);
    TrickleToDot.writeToDot(g, writer);

    writer.close();
    System.out.println(outputStream.toString());
  }
}
