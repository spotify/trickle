package com.spotify.trickle;

/**
 * Marker interface for values that are nodes. This interface is for internal use within Trickle,
 * and should never be implemented directly. Instead, you should implement one of the sub-interfaces
 * that indicate the number of parameters used for the node.
 */
public interface Node<R> extends Value<R> {
}
