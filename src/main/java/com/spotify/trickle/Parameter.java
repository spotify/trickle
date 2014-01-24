package com.spotify.trickle;

/**
 * Marker interface for a parameter of type T. Clients should never implement this interface directly,
 * only the NodeN interfaces that indicate the number of parameters a particular node needs.
 */
public interface Parameter<T> {
}
