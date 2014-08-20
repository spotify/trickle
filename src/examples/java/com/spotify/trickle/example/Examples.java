package com.spotify.trickle.example;

import com.google.common.util.concurrent.ListenableFuture;

import com.spotify.trickle.CallInfo;
import com.spotify.trickle.Func0;
import com.spotify.trickle.Func1;
import com.spotify.trickle.Graph;
import com.spotify.trickle.GraphExecutionException;
import com.spotify.trickle.Input;
import com.spotify.trickle.Func2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import static com.google.common.util.concurrent.Futures.immediateFailedFuture;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.Fallbacks.always;
import static com.spotify.trickle.Trickle.call;

/**
 * This class contains examples of how to use Trickle for combining asynchronous calls.
 */
public class Examples {

  public static final Input<String> NAME = Input.named("name");
  public static final Input<String> GREETING = Input.named("greeting");

  public static void helloWorld() throws Exception {
    Func1<String, String> transformName = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String name) {
        return immediateFuture("$$" + name);
      }
    };
    Func1<String, String> transformGreeting = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String greeting) {
        return immediateFuture(greeting + "$$$");
      }
    };
    Func2<String, String, String> combine = new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String greet, String name) {
        String result = String.format("%s %s!", greet.replaceAll("$", ""), name.replaceAll("$", ""));
        return immediateFuture(result);
      }
    };

    Graph<String> g1 = call(transformName).with(NAME).named("nameTransformer");
    Graph<String> g2 = call(transformGreeting).with(GREETING);
    Graph<String> g3 = call(combine).with(g2, g1).named("combiner");

    String s = g3
        .bind(NAME, "world")
        .bind(GREETING, "Hello")
        .run().get();
    System.out.println(s);
  }

  /**
   * Declaring the graph variables along with the nodes with the same names
   * but assign them later when composing the graph.
   * NOTE: we're not yet sure that this is a useful convention!
   *
   * @throws Exception
   */
  public static void helloWorldVariableConvention() throws Exception {
    Graph<String> transformNameG;
    Func1<String, String> transformName = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String name) {
        return immediateFuture("$$" + name);
      }
    };
    Graph<String> transformGreetingG;
    Func1<String, String> transformGreeting = new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(String greeting) {
        return immediateFuture(greeting + "$$$");
      }
    };
    Graph<String> combineG;
    Func2<String, String, String> combine = new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(String greet, String name) {
        String result = String.format("%s %s!", greet.replaceAll("$", ""), name.replaceAll("$", ""));
        return immediateFuture(result);
      }
    };

    transformNameG = call(transformName).with(NAME).named("nameTransformer");
    transformGreetingG = call(transformGreeting).with(GREETING);
    combineG = call(combine).with(transformNameG, transformGreetingG).named("combiner");

    String s = combineG
        .bind(NAME, "world")
        .bind(GREETING, "Hello")
        .run().get();
    System.out.println(s);
  }

  public static class SeparateInstantiationAndExecution {
    private final Graph<Integer> graph;

    public SeparateInstantiationAndExecution() {
      Func2<String, String, String> combineInputs = new Func2<String, String, String>() {
        @Override
        public ListenableFuture<String> run(String arg1, String arg2) {
          System.out.println(" - combining inputs");
          return immediateFuture(arg1 + " " + arg2);
        }
      };
      Func1<String, Integer> length = new Func1<String, Integer>() {
        @Override
        public ListenableFuture<Integer> run(String arg) {
          System.out.println(" - calculating lengths");
          return immediateFuture(arg.length());
        }
      };
      Func0<Void> sideTrack = new Func0<Void>() {
        @Override
        public ListenableFuture<Void> run() {
          System.out.println(" - getting sidetracked!");
          return immediateFuture(null);
        }
      };

      Graph<String> n1 = call(combineInputs).with(NAME, GREETING);
      Graph<Void> n2   = call(sideTrack);
      graph            = call(length).with(n1).after(n2);
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

      Func1<String, String> node = new Func1<String, String>() {
        @Override
        public ListenableFuture<String> run(String arg) {
          if (arg.equals("igor")) {
            throw new RuntimeException("oh noh!");
          }

          return immediateFuture("Hi " + arg);
        }
      };

      graph = call(node).with(NAME).fallback(always("Illegal name"));
    }

    public ListenableFuture<String> greet(String name) {
      return graph.bind(NAME, name).run(executor);
    }
  }


  public static class TroubleShooting {
    private final Graph<Integer> graph;

    public TroubleShooting() {
      Func1<String, String> step1 = new Func1<String, String>() {
        @Override
        public ListenableFuture<String> run(@Nullable String arg) {
          return immediateFuture("arg was: " + arg);
        }
      };
      Func1<String, String> step2 = new Func1<String, String>() {
        @Override
        public ListenableFuture<String> run(@Nullable String arg) {
          return immediateFailedFuture(new NullPointerException("expected failure"));
        }
      };
      Func2<String, String, Integer> step3 = new Func2<String, String, Integer>() {
        @Override
        public ListenableFuture<Integer> run(String arg1, String arg2) {
          return immediateFuture(arg1.length() + arg2.length());
        }
      };

      Graph<String> g1 = call(step1).with(NAME).named("step 1");
      Graph<String> g2 = call(step2).with(NAME).named("step 2");
      graph = call(step3).with(g1, g2);
    }

    public ListenableFuture<Integer> calculate(String input) {
      return graph.bind(NAME, input).run();
    }
  }

  public static void main(String[] args) throws Exception {
    System.out.println("Hello world:\n-------");
    helloWorld();

    System.out.println("\nCount length:\n-------");
    System.out.println(" length is: " + new SeparateInstantiationAndExecution().combinedLength("hi", "rouz").get());

    ExecutorService executor = Executors.newSingleThreadExecutor();

    FallbackAndExecutor fallbackAndExecutor = new FallbackAndExecutor(executor);
    System.out.println("\nFallback:\n-------");
    System.out.println(fallbackAndExecutor.greet("gof").get());
    System.out.println(fallbackAndExecutor.greet("igor").get());

    executor.shutdown();

    TroubleShooting troubleShooting = new TroubleShooting();
    System.out.println("\nTroubleshooting:\n-------");

    try {
      troubleShooting.calculate("hey").get();
    }
    catch (ExecutionException ee) {
      GraphExecutionException e = (GraphExecutionException) ee.getCause();
      System.out.println("Stack trace: ");
      e.printStackTrace(System.out);
      System.out.println("Information about calls:");
      for (CallInfo callInfo : e.getCalls()) {
        System.out.println("  " + callInfo);
      }

    }
  }
}
