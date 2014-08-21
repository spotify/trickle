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
public class ExecutionWrappingTester {

  private final Random rnd =  new SecureRandom();
  private final Input<String> input = Input.named("hi");
  private final ListeningExecutorService executorService =
      MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

  @Test
  public void shouldWorkWellForSequence() throws Exception {

    Graph<String> step1 = call(singleInput("step1")).with(input).named("step1");
    Graph<String> step2 = call(singleInput("step2")).with(step1).named("step2");
    Graph<String> step3 = call(singleInput("step3")).with(step2).named("step3");

    step3.bind(input, "hey").run().get();
  }

  @Test
  public void shouldWorkWellForFanOutFanIn() throws Exception {

    //             input
    //              step1
    //      step2a           step2b
    //  step3aa step3ab   step3ba step3bb
    //      step4a           step4b
    //              step5

    Graph<String> step1 = call(singleInput("step1")).with(input).named("step1");
    Graph<String> step2a = call(singleInput("step2a")).with(step1).named("step2a");
    Graph<String> step2b = call(singleInput("step2b")).with(step1).named("step2b");
    Graph<String> step3aa = call(singleInput("step3aa")).with(step2a).named("step3aa");
    Graph<String> step3ab = call(singleInput("step3ab")).with(step2a).named("step3ab");
    Graph<String> step3ba = call(singleInput("step3ba")).with(step2b).named("step3ba");
    Graph<String> step3bb = call(singleInput("step3bb")).with(step2b).named("step3bb");
    Graph<String> step4a = call(twoInputs("step4a")).with(step3aa, step3ab).named("step4a");
    Graph<String> step4b = call(twoInputs("step4b")).with(step3ba, step3bb).named("step4b");
    Graph<String> step5 = call(twoInputs("step5")).with(step4a, step4b).named("step5");

    step5.bind(input, "hey").run().get();
  }

  private Func1<String, String> singleInput(final String name) {
    return new Func1<String, String>() {
      @Override
      public ListenableFuture<String> run(@Nullable final String arg) {
        return executorService.submit(new Callable<String>() {
          @Override
          public String call() throws Exception {
            System.out.println("call " + name + "(" + arg + ")");
            try {
              Thread.sleep(rnd.nextInt(40));
              System.out.println("slept " + name);

              if (rnd.nextInt(100) < 30) {
                System.out.println("failed " + name);
                throw new RuntimeException(name + " failed!");
              }
            } catch (InterruptedException e) {
              throw new RuntimeException("interrupted", e);
            }

            System.out.println("done " + name);
            return name;
          }
        });
      }
    };
  }

  private Func2<String, String, String> twoInputs(final String name) {
    return new Func2<String, String, String>() {
      @Override
      public ListenableFuture<String> run(@Nullable final String arg1, @Nullable final String arg2) {
        return executorService.submit(new Callable<String>() {
          @Override
          public String call() throws Exception {
            System.out.println("call " + name + "(" + arg1 + ", " + arg2 + ")");
            try {
              Thread.sleep(rnd.nextInt(40));
              System.out.println("slept " + name);

              if (rnd.nextInt(100) < 40) {
                System.out.println("failed " + name);
                throw new RuntimeException(name + " failed!");
              }
            } catch (InterruptedException e) {
              throw new RuntimeException("interrupted", e);
            }

            System.out.println("done " + name);
            return name;
          }
        });
      }
    };
  }

}
