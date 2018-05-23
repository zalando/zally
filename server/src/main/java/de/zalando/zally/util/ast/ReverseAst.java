package de.zalando.zally.util.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReverseAst holds meta information for nodes of a Swagger or OpenApi object.
 */
public class ReverseAst<T> {
    /**
     * Creates a new instance of ReverseAstBuilder from a Swagger or OpenApi object.
     *
     * @param root Swagger or OpenApi instance.
     * @return ReverseAstBuilder instance.
     */
    @Nonnull
    public static <T> ReverseAstBuilder<T> fromObject(@Nonnull T root) {
        return new ReverseAstBuilder<>(root);
    }

    private final Map<Object, Node> objectsToNodes;
    private final Map<String, Node> pointersToNodes;

    ReverseAst(Map<Object, Node> objectsToNodes, Map<String, Node> pointersToNodes) {
        this.objectsToNodes = objectsToNodes;
        this.pointersToNodes = pointersToNodes;
    }

    @Nullable
    public String getPointer(Object key) {
        Node node = this.objectsToNodes.get(key);
        if (node != null) {
            return node.pointer;
        }
        return null;
    }

    public boolean isIgnored(String pointer, String ignoreValue) {
        return isIgnored(this.pointersToNodes.get(pointer), ignoreValue);
    }

    private boolean isIgnored(Node node, String ignoreValue) {
        if (node == null) {
            return false;
        }
        boolean ignored = isIgnored(node.marker, ignoreValue);
        return ignored ||
            (node.hasChildren() && node.getChildren().parallelStream().allMatch(c -> isIgnored(c.marker, ignoreValue)));
    }

    private boolean isIgnored(Marker marker, String ignoreValue) {
        return marker != null && Marker.TYPE_X_ZALLY_IGNORE.equals(marker.type) && marker.values.contains(ignoreValue);
    }

    public Collection<String> getIgnoreValues(String pointer) {
        return getIgnoreValues(this.pointersToNodes.get(pointer));
    }

    private Collection<String> getIgnoreValues(Node node) {
        if (node == null) {
            return Collections.emptySet();
        }
        Collection<String> markers = getIgnoreValues(node.marker);
        if (!markers.isEmpty()) {
            return markers;
        }
        return node
            .getChildren()
            .parallelStream()
            .flatMap(child -> getIgnoreValues(child.marker).stream())
            .collect(Collectors.toSet());
    }

    private Collection<String> getIgnoreValues(Marker marker) {
        if (marker != null && Marker.TYPE_X_ZALLY_IGNORE.equals(marker.type)) {
            return marker.values;
        }
        return Collections.emptySet();
    }
}
