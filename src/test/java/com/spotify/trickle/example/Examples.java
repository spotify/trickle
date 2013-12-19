package com.spotify.trickle.example;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.spotify.trickle.Graph;
import com.spotify.trickle.Name;
import com.spotify.trickle.Node1;
import com.spotify.trickle.Node2;
import com.spotify.trickle.Trickle;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * This class contains examples of how to use Trickle for combining asynchronous calls.
 */
public class Examples {

  public static final Name<String> NAME = Name.named("name", String.class);
  public static final Name<String> GREETING = Name.named("greeting", String.class);

  public static void helloWorld() throws Exception {
    Node1<String, String> transformName = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String name) {
        return immediateFuture("$$" + name);
      }
    };
    Node1<String, String> transformGreeting = new Node1<String, String>() {
      @Override
      public ListenableFuture<String> run(String greeting) {
        return immediateFuture(greeting + "$$$");
      }
    };
    Node2<String, String, String> combine = new Node2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String transformedGreet, String transformedName) {
        return Futures.immediateFuture(String.format("%s %s!", transformedGreet.replaceAll("$", ""), transformedName.replaceAll("$", "")));
      }
    };

    Graph<String> graph = Trickle.graph(String.class)
        .inputs(GREETING, NAME)
        .call(transformName).with(NAME)
        .call(transformGreeting).with(GREETING)
        .call(combine).with(GREETING, NAME)
        .output(combine);


    System.out.println(graph.bind(NAME, "world").bind(GREETING, "Hello").run().get());
  }

  public static class SeparateInstantiationAndExecution {
    private final Graph<Integer> graph;

    public SeparateInstantiationAndExecution() {
      Node2<String, String, String> combineInputs = new Node2<String, String, String>() {
        @Override
        public ListenableFuture<String> run(String arg1, String arg2) {
          return immediateFuture(arg1 + " " + arg2);
        }
      };
      Node1<String, Integer> length = new Node1<String, Integer>() {
        @Override
        public ListenableFuture<Integer> run(String arg) {
          return immediateFuture(arg.length());
        }
      };


      graph = Trickle.graph(Integer.class)
          .inputs(NAME, GREETING)
          .call(combineInputs).with(NAME, GREETING)
          .call(length).with(combineInputs)
          .output(length);
    }

    public ListenableFuture<Integer> combinedLength(String name, String greeting) {
      return graph.bind(GREETING, greeting).bind(NAME, name).run();
    }
  }

  public static class FallbackAndExecutor {
    private final Executor executor;
    private final Graph<String> graph;

    public FallbackAndExecutor(Executor executor) {
      this.executor = executor;

      Node1<String, String> node = new Node1<String, String>() {
        @Override
        public ListenableFuture<String> run(String arg) {
          if (arg.equals("igor")) {
            throw new RuntimeException("oh noh!");
          }

          return immediateFuture("Hi " + arg);
        }
      };

      graph = Trickle.graph(String.class)
          .inputs(NAME)
          .call(node).with(NAME).fallback("Illegal name")
          .output(node);
    }

    public ListenableFuture<String> greet(String name) {
      return graph.bind(NAME, name).run(executor);
    }
  }


  public static void main(String[] args) throws Exception {
    System.out.println("Hello world:\n-------");
    helloWorld();

    System.out.println("Count length:\n-------");
    System.out.println(new SeparateInstantiationAndExecution().combinedLength("hi", "rouz").get());

    System.out.println("Fallback:\n-------");
    System.out.println(new FallbackAndExecutor(Executors.newSingleThreadExecutor()).greet("gof").get());
    System.out.println(new FallbackAndExecutor(Executors.newSingleThreadExecutor()).greet("igor").get());
  }
}
