package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static com.spotify.trickle.Trickle.call;

public class TrickleToDotTest {

  @Test
  public void shouldGenerateDot() throws Exception {
    // this isn't a proper test, but then the TrickleToDot class isn't a proper class either.
    Func0<String> node0 = new Func0<String>() {
      @Override
      public ListenableFuture<String> run() {
        throw new UnsupportedOperationException();
      }
    };
    Func2<String, String, String> node1 = new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg, String arg2) {
        throw new UnsupportedOperationException();
      }
    };
    Func3<String, String, String, String> node2 = new Func3<String, String, String, String>() {
      @Override
      public ListenableFuture<String> run(String arg, String arg2, String arg3) {
        throw new UnsupportedOperationException();
      }
    };
    Func1<String, String> node3 = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String arg) {
        throw new UnsupportedOperationException();
      }
    };

    Name<String> first = Name.named("first input", String.class);
    Name<String> second = Name.named("second input", String.class);

    Graph<String> g0 = call(node0).named("node 0");
    Graph<String> g1 = call(node1).with(g0, first).named("node 1");
    Graph<String> g2 = call(node2).with(g1, g0, second).named("node 2");
    Graph<String> g = call(node3).with(second).after(g1, g2);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(outputStream);
    TrickleToDot.writeToDot(g, writer);

    writer.close();
    System.out.println(outputStream.toString());
  }
}
