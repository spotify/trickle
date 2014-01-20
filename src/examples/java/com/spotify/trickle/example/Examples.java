package com.spotify.trickle.example;

import com.google.common.util.concurrent.ListenableFuture;
import com.spotify.trickle.Graph;
import com.spotify.trickle.Name;
import com.spotify.trickle.Node0;
import com.spotify.trickle.Node1;
import com.spotify.trickle.Node2;
import com.spotify.trickle.Trickle;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.Trickle.always;

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
      public ListenableFuture<String> run(String greet, String name) {
        String result = String.format("%s %s!", greet.replaceAll("$", ""), name.replaceAll("$", ""));
        return immediateFuture(result);
      }
    };

    Graph<String> graph = Trickle.graph(String.class)
        .call(transformName).with(NAME).named("nameTransformer")
        .call(transformGreeting).with(GREETING)
        .finallyCall(combine).with(transformGreeting, transformName).named("combiner")
        .build();

    System.out.println(graph.bind(NAME, "world").bind(GREETING, "Hello").run().get());
  }

  public static class SeparateInstantiationAndExecution {
    private final Graph<Integer> graph;

    public SeparateInstantiationAndExecution() {
      Node2<String, String, String> combineInputs = new Node2<String, String, String>() {
        @Override
        public ListenableFuture<String> run(String arg1, String arg2) {
          System.out.println(" - combining inputs");
          return immediateFuture(arg1 + " " + arg2);
        }
      };
      Node1<String, Integer> length = new Node1<String, Integer>() {
        @Override
        public ListenableFuture<Integer> run(String arg) {
          System.out.println(" - calculating lengths");
          return immediateFuture(arg.length());
        }
      };
      Node0<Void> sideTrack = new Node0<Void>() {
        @Override
        public ListenableFuture<Void> run() {
          System.out.println(" - getting sidetracked!");
          return immediateFuture(null);
        }
      };

      graph = Trickle.graph(Integer.class)
          .call(combineInputs).with(NAME, GREETING)
          .call(sideTrack)
          .finallyCall(length).with(combineInputs).after(sideTrack)
          .build();
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
          .finallyCall(node).with(NAME).fallback(always("Illegal name"))
          .build();
    }

    public ListenableFuture<String> greet(String name) {
      return graph.bind(NAME, name).run(executor);
    }
  }


  public static void main(String[] args) throws Exception {
    System.out.println("Hello world:\n-------");
    helloWorld();

    System.out.println("\nCount length:\n-------");
    System.out.println(" length is: " + new SeparateInstantiationAndExecution().combinedLength("hi", "rouz").get());

    ExecutorService executor = Executors.newSingleThreadExecutor();

    System.out.println("\nFallback:\n-------");
    System.out.println(new FallbackAndExecutor(executor).greet("gof").get());
    System.out.println(new FallbackAndExecutor(executor).greet("igor").get());

    executor.shutdown();
  }
}
