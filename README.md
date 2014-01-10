trickle
=======

A simple library for composing asynchronous code. The main reason for it to exist is to make it
easier to create graphs of interconnected asynchronous calls, pushing the 'worrying about
concurrency' aspects into the framework rather than mixing it in with the business logic.

See [Examples.java](src/examples/java/com/spotify/trickle/Examples.java)

Caveats:
- The current implementation is mostly focused on getting the API right; feel free to check out the
internal implementation, but be aware of the fact that it's quite hacky.
- I want to get rid of the need to define the 'output' node by adding a requirement that there
should be a single sink node in the graph; this sink node would have to return the correct type.
