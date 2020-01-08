[![Maven Central](https://img.shields.io/maven-central/v/com.spotify/trickle.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.spotify%22%20trickle*)
[![License](https://img.shields.io/github/license/spotify/trickle.svg)](LICENSE.txt)

trickle
=======

A small library for composing asynchronous code. The main reason for it to exist is to make it
easier to create graphs of interconnected asynchronous calls, pushing the 'worrying about
concurrency' aspects into the framework rather than mixing it in with the business logic.

NOTE: this project is no longer supported. Feel free to fork it if you like it. Internally, we've 
switched to using CompletionStage/CompletableFuture instead.

# When should I use it?


- ~~When you are combining more than 2 asynchronous calls together, and you think the code is hard to read.~~
- ~~When you want to separate concurrency management aspects (when code does something) from business logic (what it does).~~
- ~~When you want to use something smaller and easier to learn than frameworks like Akka and RxJava.~~

Probably none of the above, see note at top.

# Getting Started

Include the latest version of Trickle into your project:

```xml
<dependency>
  <groupId>com.spotify</groupId>
  <artifactId>trickle</artifactId>
  <version>0.6.2</version>
</dependency>
```

Define the input parameters to your call graph:

```java
public static final Input<String> KEYWORD = Input.named("keyword");
public static final Input<String> ARTIST = Input.named("artist");
```

Define the code to be executed in the nodes of your graph:

```java
Func1<String, List<Track>> findTracks = new Func1<String, List<Track>>() {
  @Override
  public ListenableFuture<List<Track>> run(String keyword) {
    return search.findTracks(keyword);
  }
};
Func1<String, Artist> findArtist = new Func1<String, Artist>() {
  @Override
  public ListenableFuture<Artist> run(String artistName) {
    return metadata.lookupArtist(artistName);
  }
};
Func2<Artist, List<Track>, MyOutput> combine = new Func2<Artist, List<Track>, MyOutput>() {
  @Override
  public ListenableFuture<MyOutput> run(Artist artist, List<Track> tracks) {
    return Futures.immediateFuture(new MyOutput(artist, tracks));
  }
};
```

Wire up your call graph:

```java
Graph<List<Track>> tracks = Trickle.call(findTracks).with(KEYWORD).fallback(emptyList());
Graph<Artist> artist = Trickle.call(findArtist).with(ARTIST);
this.output = Trickle.call(combine).with(artist, tracks);
```

Note that the ```findTracks``` node has been given a fallback, so an empty list of tracks will 
be used if the call to find tracks throws an exception. This way, you can get graceful degradation
in case of partial failure.

At some later stage, call the graph for some specific keyword and artist name:

```java
public ListenableFuture<MyOutput> doTheThing(String keyword, String artistName) {
  return this.output.bind(KEYWORD, keyword).bind(ARTIST, artistName).run();
}
```

See [`Examples.java`](src/examples/java/com/spotify/trickle/example/Examples.java) for more examples
and see the wiki for more in-depth descriptions of the library.


# Notes about maturity

We're using Trickle internally at Spotify in core, production-critical services that would break 
Spotify completely if they failed. This means we have a fairly high degree of confidence that it
works. It is, however, a young library and you shouldn't be surprised if there are API changes
in the next few months.

# Code of conduct
This project adheres to the [Open Code of Conduct][code-of-conduct]. By participating, you are expected to honor this code.

[code-of-conduct]: https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md
