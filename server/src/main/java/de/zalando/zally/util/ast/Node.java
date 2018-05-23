package de.zalando.zally.util.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;

/**
 * A stack node for tree-traversal.
 */
final class Node {
    private Node parent;
    private Collection<Node> children;
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

    Node setParent(Node parent) {
        this.parent = parent;
        return this;
    }

    Node setChildren(Collection<Node> children) {
        this.children = children;
        return this;
    }

    @Nullable
    Node getParent() {
        return parent;
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
