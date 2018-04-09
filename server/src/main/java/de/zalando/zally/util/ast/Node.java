package de.zalando.zally.util.ast;

/**
 * A stack node for tree-traversal.
 */
final class Node {
    final Object object;
    final String pointer;
    final Marker marker;
    final boolean skip;

    Node(Object object, String pointer, Marker marker) {
        this(object, pointer, marker, false);
    }

    Node(Object object, String pointer, Marker marker, boolean skip) {
        this.object = object;
        this.pointer = pointer;
        this.skip = skip;
        this.marker = marker;
    }
}
