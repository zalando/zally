package de.zalando.zally.util.ast;

import com.fasterxml.jackson.core.JsonPointer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedList;

/**
 * A stack node for tree-traversal.
 */
final class Node {
    private Collection<Node> children;
    protected final Object object;
    protected final JsonPointer pointer;
    final Marker marker;
    final boolean skip;

    Node(Object object, JsonPointer pointer, Marker marker) {
        this(object, pointer, marker, false);
    }

    Node(Object object, JsonPointer pointer, Marker marker, boolean skip) {
        this.object = object;
        this.pointer = pointer;
        this.skip = skip;
        this.marker = marker;
    }

    Node setChildren(Collection<Node> children) {
        this.children = children;
        return this;
    }

    @Nonnull
    Collection<Node> getChildren() {
        if (children == null) {
            return new LinkedList<>();
        }
        return children;
    }

    boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
