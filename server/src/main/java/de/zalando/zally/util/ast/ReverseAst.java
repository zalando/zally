package de.zalando.zally.util.ast;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * ReverseAst holds meta information for nodes of a Swagger or OpenApi object.
 */
public class ReverseAst {
    /**
     * Creates a new instance of ReverseAstBuilder from a Swagger or OpenApi object.
     *
     * @param root Swagger or OpenApi instance.
     * @return ReverseAstBuilder instance.
     */
    public static ReverseAstBuilder fromObject(Object root) {
        return new ReverseAstBuilder(root);
    }

    private final Map<Object, Node> map;

    ReverseAst(Map<Object, Node> map) {
        this.map = map;
    }

    @Nullable
    public Node getNode(Object key) {
        return this.map.get(key);
    }

    /**
     * Find a pointer for a given Node (typically from another AST).
     * <p>
     * A pointer can be found if the object value of the given root node or
     * of any of its children exists in this AST.
     *
     * @param key Node and object value from another AST.
     * @return JSON pointer or null.
     */
    @Nullable
    public String getPointer(Node key) {
        Node nodeForKey = this.map.get(key.object);
        if (nodeForKey != null) {
            return nodeForKey.pointer;
        }

        Deque<Collection<Node>> siblings = new LinkedList<>();
        siblings.push(key.getChildren());
        int depth = 0; // count how many layers of siblings we have descended

        while (!siblings.isEmpty()) {
            depth++;
            Collection<Node> children = siblings.pop();

            for (Node child : children) {
                Node childNode = this.map.get(child.object);
                if (childNode != null) {
                    // we have found a matching object value for a nested child node of the original key node
                    // now we need to ascend the chain of nodes back up for as many layers of siblings as we descended
                    while (depth > 0 && childNode.parent != null) {
                        depth--;
                        childNode = childNode.parent;
                    }
                    return childNode.pointer;
                }
                siblings.push(child.getChildren());
            }
        }
        // Fall back on converting the input pointer.
        return JsonPointers.convertPointer(key.pointer);
    }

    @Nullable
    public String getPointer(Object key) {
        Node node = this.map.get(key);
        if (node != null) {
            return node.pointer;
        }
        return null;
    }

    public boolean isIgnored(Object key) {
        Node node = this.map.get(key);
        return node != null && node.marker != null && Marker.TYPE_X_ZALLY_IGNORE.equals(node.marker.type);
    }

    @Nullable
    public String getIgnoreValue(Object key) {
        Node node = this.map.get(key);
        if (node != null && node.marker != null && Marker.TYPE_X_ZALLY_IGNORE.equals(node.marker.type)) {
            return node.marker.value;
        }
        return null;
    }
}
