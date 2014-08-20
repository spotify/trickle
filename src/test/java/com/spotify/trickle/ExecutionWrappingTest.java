/*
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version 
 * 2.0 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 */

package com.spotify.trickle;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import static com.spotify.trickle.Trickle.call;

/**
 * TODO: document!
 */
public class ExecutionWrappingTest {

  private final Random rnd =  new SecureRandom();
  private final Input<String> input = Input.named("hi");
  private final ListeningExecutorService executorService =
      MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

  @Test
  public void shouldWorkWellForSequence() throws Exception {

    Graph<String> step1 = call(singleInput()).with(input).named("step1");
    Graph<String> step2 = call(singleInput()).with(step1).named("step2");
    Graph<String> step3 = call(singleInput()).with(step2).named("step3");

    step3.bind(input, "hey").run().get();
  }

  @Test
  public void shouldWorkWellForFanOutFanIn() throws Exception {

    //       input
    //        step1
    //   step2a     step2b
    //   step3a     step3b
    //        step4

    Graph<String> step1 = call(singleInput()).with(input).named("step1");
    Graph<String> step2a = call(singleInput()).with(step1).named("step2a");
    Graph<String> step2b = call(singleInput()).with(step1).named("step2b");
    Graph<String> step3a = call(singleInput()).with(step2a).named("step3a");
    Graph<String> step3b = call(singleInput()).with(step2b).named("step3b");
    Graph<String> step4 = call(twoInputs()).with(step3a, step3b).named("step4");

    step4.bind(input, "hey").run().get();
  }

  private Func1<String, String> singleInput() {
    return new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(@Nullable final String arg) {
        return executorService.submit(new Callable<String>() {
          @Override
          public String call() throws Exception {
            System.out.println("call " + arg);
            try {
              Thread.sleep(rnd.nextInt(40));
              System.out.println("slept " + arg);

              if (rnd.nextInt(100) < 40) {
                System.out.println("failed " + arg);
                throw new RuntimeException("failed!");
              }
            } catch (InterruptedException e) {
              throw new RuntimeException("interrupted", e);
            }

            System.out.println("done " + arg);
            return arg + " - ok";
          }
        });
      }
    };
  }

  private Func2<String, String, String> twoInputs() {
    return new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(@Nullable final String arg1, @Nullable final String arg2) {
        return executorService.submit(new Callable<String>() {
          @Override
          public String call() throws Exception {
            System.out.println("call " + arg1 + ", " + arg2);
            try {
              Thread.sleep(rnd.nextInt(40));
              System.out.println("slept " + arg1 + ", " + arg2);

              if (rnd.nextInt(100) < 40) {
                System.out.println("failed " + arg1 + ", " + arg2);
                throw new RuntimeException("failed!");
              }
            } catch (InterruptedException e) {
              throw new RuntimeException("interrupted", e);
            }

            System.out.println("done " + arg1 + ", " + arg2);
            return arg1 + ", " + arg2 + " - ok";
          }
        });
      }
    };
  }

}
