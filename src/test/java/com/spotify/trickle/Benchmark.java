package com.spotify.trickle;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.profile.ProfilerType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import rx.Observable;
import rx.util.functions.Func1;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.spotify.trickle.Trickle.call;

/**
 * Runs benchmarks using JMH: http://openjdk.java.net/projects/code-tools/jmh/.
 */
public class Benchmark {
  private static boolean useExecutor = false;

  private static ListenableFuture<Long> heartbeatIntervalMillis(ListeningExecutorService executor) {
    if (useExecutor) {
      return executor.submit(new Callable<Long>() {
        @Override
        public Long call() throws Exception {
          return System.currentTimeMillis();
        }
      });
    }

    return immediateFuture(System.currentTimeMillis());
  }

  private static ListenableFuture<Void> updateSerialCall(ListeningExecutorService executor) {
    if (useExecutor) {
      return executor.submit(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          return null;
        }
      });
    }

    return immediateFuture(null);
  }

  private static ListenableFuture<Boolean> putHeartbeat(final String arg, ListeningExecutorService executor) {
    if (useExecutor) {
      return executor.submit(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          return arg.hashCode() % 2 == 0;
        }
      });
    }

    return immediateFuture(arg.hashCode() % 2 == 0);
  }

  private static ListenableFuture<Integer> fetchEndpoint(final String arg, ListeningExecutorService executor) {
    if (useExecutor) {
      return executor.submit(new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
          return arg.length();
        }
      });
    }

    return immediateFuture(arg.length());
  }


  public static final Name<String> HEARTBEAT_ENDPOINT = new Name<String>("heartbeat", String.class);

  @State(Scope.Thread)
  public static class TrickleGraph {
    ListeningExecutorService executor;

    @Setup
    public void setupExecutor() {
      executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
    }

    @TearDown
    public void shutdownExecutor() {
      executor.shutdown();
      executor = null;
    }


    Node1<String, Integer> getEndpoint =
        new Node1<String, Integer>() {
          @Override
          public ListenableFuture<Integer> run(String arg) {
            return fetchEndpoint(arg, executor);
          }
        };
    Node1<String, Boolean> putHeartbeatEntry = new Node1<String, Boolean>() {
      @Override
      public ListenableFuture<Boolean> run(String arg) {
        return putHeartbeat(arg, executor);
      }
    };
    Node1<Integer, Void> updateSerial =
        new Node1<Integer, Void>() {
          @Override
          public ListenableFuture<Void> run(Integer arg) {
            return updateSerialCall(executor);
          }
        };
    Node0<Long> resultNode = new Node0<Long>() {
      @Override
      public ListenableFuture<Long> run() {
        return heartbeatIntervalMillis(executor);
      }
    };

    Graph<Integer> endpoint = call(getEndpoint).with(HEARTBEAT_ENDPOINT);
    Graph<Boolean> putHeartbeat = call(putHeartbeatEntry).with(HEARTBEAT_ENDPOINT).after(endpoint);
    Graph<Void> serial = call(updateSerial).with(endpoint).after(putHeartbeat);
    Graph<Long> result = call(resultNode).after(serial);
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(Mode.Throughput)
  public long benchmarkImmediateTrickle(TrickleGraph graph) throws ExecutionException, InterruptedException {
    useExecutor = false;
    return runTrickle(graph);
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(Mode.Throughput)
  public long benchmarkExecutorTrickle(TrickleGraph graph) throws ExecutionException, InterruptedException {
    useExecutor = true;
    return runTrickle(graph);
  }

  private long runTrickle(TrickleGraph graph) throws InterruptedException, ExecutionException {
    return graph.result.bind(HEARTBEAT_ENDPOINT, String.valueOf(System.currentTimeMillis())).run().get();
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(Mode.Throughput)
  public long benchmarkImmediateGuava(final TrickleGraph graph) throws ExecutionException, InterruptedException {
    useExecutor = false;
    return runGuava(graph);
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(Mode.Throughput)
  public long benchmarkExecutorGuava(final TrickleGraph graph) throws ExecutionException, InterruptedException {
    useExecutor = true;
    return runGuava(graph);
  }

  private long runGuava(final TrickleGraph graph) throws InterruptedException, ExecutionException {
    final String hey = String.valueOf(System.currentTimeMillis());

    ListenableFuture<Integer> endpointFuture = fetchEndpoint(hey, graph.executor);
    ListenableFuture<Boolean> heartbeatFuture = Futures.transform(endpointFuture, new AsyncFunction<Integer, Boolean>() {
      @Override
      public ListenableFuture<Boolean> apply(Integer input) throws Exception {
        return putHeartbeat(hey, graph.executor);
      }
    });
    ListenableFuture<Void> serial = Futures.transform(heartbeatFuture, new AsyncFunction<Boolean, Void>() {
      @Override
      public ListenableFuture<Void> apply(Boolean input) throws Exception {
        return updateSerialCall(graph.executor);
      }
    });

    ListenableFuture<Long> result = Futures.transform(serial, new AsyncFunction<Void, Long>() {
      @Override
      public ListenableFuture<Long> apply(Void input) throws Exception {
        return heartbeatIntervalMillis(graph.executor);
      }
    });

    return result.get();
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(Mode.Throughput)
  public long benchmarkImmediateRx(final TrickleGraph graph) {
    useExecutor = false;
    return runRx(graph);
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(Mode.Throughput)
  public long benchmarkExecutorRx(final TrickleGraph graph) {
    useExecutor = true;
    return runRx(graph);
  }

  private long runRx(final TrickleGraph graph) {
    final String hey = String.valueOf(System.currentTimeMillis());

    return Observable
        .from(fetchEndpoint(hey, graph.executor))
        .flatMap(new Func1<Integer, Observable<? extends Boolean>>() {
          @Override
          public Observable<? extends Boolean> call(Integer integer) {
            return Observable.from(putHeartbeat(hey, graph.executor));
          }
        }).flatMap(new Func1<Boolean, Observable<?>>() {
          @Override
          public Observable<?> call(Boolean aBoolean) {
            return Observable.from(updateSerialCall(graph.executor));
          }
        }).flatMap(new Func1<Object, Observable<? extends Long>>() {
          @Override
          public Observable<? extends Long> call(Object o) {
            return Observable.from(heartbeatIntervalMillis(graph.executor));
          }
        }).toBlockingObservable().single();
  }


  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(".*" + Benchmark.class.getSimpleName() + ".*")
        .warmupIterations(3)
        .measurementIterations(5)
        .forks(1)
//        .addProfiler(ProfilerType.STACK)
//        .addProfiler(ProfilerType.HS_RT)
        .build();

    new Runner(opt).run();
  }
}
