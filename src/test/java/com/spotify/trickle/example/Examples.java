/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle.example;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.spotify.trickle.Node;
import com.spotify.trickle.Trickle;

import static com.spotify.trickle.Name.named;
import static com.spotify.trickle.Trickle.G;

final class Examples {

  /**
   * TODO: describe example
   *
   * Node{1} (class java.lang.String)
   *   `-> Node{0} (class java.lang.String)
   *        |-> Value(Hello)
   *        `-> Binding(Name{who}) (class java.lang.String)
   */
  private static class Example1 implements Example {
    Trickle.Dep<String> who = Trickle.binding("who", String.class);

    Node<String> helloWorld = G()
        .in("Hello", String.class)
        .bind(who)
        .apply(String.class, new Object() {
          public String _(String a, String b) {
            return a + ' ' + b + '!';
          }
        });

    Node<String> withLength = G()
        .in(helloWorld)
        .apply(String.class, new Object() {
          public String _(String a) {
            return "'" + a + "' length = " + a.length();
          }
        });

    @Override
    public Node<String> root() { return withLength; }
  }

  /**
   * TODO: describe example
   *
   * Node{3} (class java.lang.String)
   *   |-> Value(Hello)
   *   [fallback]
   *   `-> Node{2} (class java.lang.String)
   *        `-> Value(default failure value)
   */
  private static class Example2 implements Example {
    Node<String> failingHello = G()
        .in("Hello", String.class)
        .fallback("default failure value", String.class)
        .apply(String.class, new Object() {
          public String _(String a) {
            throw new RuntimeException();
          }
        });

    @Override
    public Node<String> root() { return failingHello; }
  }

  /**
   * TODO: describe example
   *
   * Node{5} (class java.lang.String)
   *   |-> Binding(Name{x}) (class java.lang.Integer)
   *   [fallback]
   *   `-> Node{4} (class java.lang.String)
   *        `-> Binding(Name{x}) (class java.lang.Integer)
   */
  private static class Example3 implements Example {
    Trickle.Dep<Integer> x = Trickle.binding("x", Integer.class);

    Node<String> alternateGraph = G()
        .bind(x)
        .apply(String.class, new Object() {
          public String _(Integer x) {
            return "I am more lenient about " + x;
          }
        });

    Node<String> helloWorld = G()
        .bind(x)
        .fallback(alternateGraph)
        .apply(String.class, new Object() {
          public String _(Integer x) {
            if (x < 8) {
              throw new RuntimeException("Nooo way!");
            }

            return "Ok, I'm fine with " + x;
          }
        });

    @Override
    public Node<String> root() { return helloWorld; }
  }


  public static void main(String[] args) {
    {
      Example example = new Example1();
      example.root().printTree();
      ListenableFuture<String> result = example.root().prepare()
          .bind(named("who"), "John Doe")
          .future();

      System.out.println("run example 1");
      System.out.println(Futures.getUnchecked(result));
      /*
        run example 1
        'Hello John Doe!' length = 15
       */
    }

    {
      Example example = new Example2();
      example.root().printTree();
      ListenableFuture<String> result = example.root().prepare()
          .future();

      System.out.println("run example 2");
      System.out.println(Futures.getUnchecked(result));
      /*
        run example 2
        default failure value
       */
    }

    {
      Example example = new Example3();
      ListenableFuture<String> result1 = example.root().prepare()
          .bind(named("x"), 5)
          .future();

      System.out.println("run example 3.1");
      System.out.println(Futures.getUnchecked(result1));
      /*
        run example 3.1
        I am more lenient about 5
       */

      ListenableFuture<String> result2 = example.root().prepare()
          .bind(named("x"), 10)
          .future();

      System.out.println("run example 3.2");
      System.out.println(Futures.getUnchecked(result2));
      /*
        run example 3.2
        Ok, I'm fine with 10
       */
    }
  }


  private interface Example {
    Node<String> root();
  }
}
