package de.zalando.zally.util.ast;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonPointer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.zalando.zally.util.ast.Util.PRIMITIVES;

public class ReverseAstBuilder<T> {
    private Collection<String> extensionMethodNames = new HashSet<>();

    public static class ReverseAstException extends Exception {
        ReverseAstException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final Deque<Node> nodes = new LinkedList<>();
    private final Map<Object, Node> objectsToNodes = new IdentityHashMap<>();
    private final Map<String, Node> pointersToNodes = new HashMap<>();

    ReverseAstBuilder(T root) {
        nodes.push(new Node(root, JsonPointers.empty(), null));
    }

    public ReverseAstBuilder<T> withExtensionMethodNames(String... names) {
        this.extensionMethodNames.addAll(Arrays.asList(names));
        return this;
    }

    /**
     * Construct a new ReverseAst instance from the root object in this builder.
     * Traverses a Swagger or OpenApi object tree and constructs a map of object nodes to meta information objects.
     *
     * @return A new ReverseAst instance.
     * @throws ReverseAstException If an error occurs during reflection.
     */
    @Nonnull
    public ReverseAst build() throws ReverseAstException {
        while (!nodes.isEmpty()) {
            Node node = nodes.pop();

            if (!PRIMITIVES.contains(node.object.getClass())) {
                Collection<Node> children;
                if (node.object instanceof Map) {
                    children = handleMap((Map<?, ?>) node.object, node.pointer, node.marker);
                } else if (node.object instanceof List) {
                    children = handleList((List<?>) node.object, node.pointer, node.marker);
                } else if (node.object instanceof Set) {
                    children = handleSet((Set<?>) node.object, node.pointer, node.marker);
                } else if (node.object instanceof Object[]) {
                    children = handleArray((Object[]) node.object, node.pointer, node.marker);
                } else {
                    children = handleObject(node.object, node.pointer, node.marker);
                }
                for (Node child : children) {
                    nodes.push(child);
                }
                node.setChildren(children);
            }
            if (!node.skip) {
                objectsToNodes.put(node.object, node);
                pointersToNodes.put(node.pointer.toString(), node);
            }
        }
        return new ReverseAst(objectsToNodes, pointersToNodes);
    }

    private Deque<Node> handleMap(Map<?, ?> map, JsonPointer pointer, Marker defaultMarker) {
        final Deque<Node> nodes = new LinkedList<>();
        final Marker marker = getMarker(map).orElse(defaultMarker);

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key instanceof String && value != null) {
                JsonPointer newPointer = pointer.append(JsonPointers.escape((String) key));
                nodes.push(new Node(value, newPointer, marker));
            }
        }
        return nodes;
    }

    private Deque<Node> handleList(List<?> list, JsonPointer pointer, Marker marker) {
        return handleArray(list.toArray(), pointer, marker);
    }

    private Deque<Node> handleSet(Set<?> set, JsonPointer pointer, Marker marker) {
        return handleArray(set.toArray(), pointer, marker);
    }

    private Deque<Node> handleArray(Object[] objects, JsonPointer pointer, Marker marker) {
        final Deque<Node> nodes = new LinkedList<>();

        for (int i = 0; i < objects.length; i++) {
            Object value = objects[i];
            if (value != null) {
                JsonPointer newPointer = pointer.append(JsonPointers.escape(String.valueOf(i)));
                nodes.push(new Node(value, newPointer, marker));
            }
        }
        return nodes;
    }

    private Deque<Node> handleObject(Object object, JsonPointer pointer, Marker defaultMarker) throws ReverseAstException {
        final Deque<Node> nodes = new LinkedList<>();
        final Marker marker = getMarker(object).orElse(defaultMarker);

        for (Method m : object.getClass().getDeclaredMethods()) {
            String name = m.getName();
            // Find all public getter methods.
            if (isPublicGetterMethod(m)) {
                try {
                    Object value = m.invoke(object);
                    if (value != null) {
                        if (m.isAnnotationPresent(JsonAnyGetter.class)) {
                            // A `JsonAnyGetter` method is simply a wrapper for nested properties.
                            // We must not use the method name but re-use the current pointer.
                            nodes.push(new Node(value, pointer, marker, /* skip */true));
                        } else {
                            JsonPointer newPointer = pointer.append(JsonPointers.escape(m));
                            nodes.push(new Node(value, newPointer, marker));
                        }
                    }
                } catch (ReflectiveOperationException e) {
                    String message = String.format("Error invoking %s on %s at path %s", name, object.getClass(), pointer);
                    throw new ReverseAstException(message, e);
                }
            }
        }
        return nodes;
    }

    private boolean isPublicGetterMethod(Method m) {
        return m.getName().startsWith("get")
            && m.getParameterCount() == 0
            && Modifier.isPublic(m.getModifiers())
            && !m.isAnnotationPresent(JsonIgnore.class);
    }

    private Optional<Marker> getMarker(Map<?, ?> map) {
        return Optional
            .ofNullable(getVendorExtensions(map, Marker.TYPE_X_ZALLY_IGNORE))
            .map(values -> new Marker(Marker.TYPE_X_ZALLY_IGNORE, values));
    }

    private Optional<Marker> getMarker(Object object) throws ReverseAstException {
        return Optional
            .ofNullable(getVendorExtensions(object, Marker.TYPE_X_ZALLY_IGNORE))
            .map(values -> new Marker(Marker.TYPE_X_ZALLY_IGNORE, values));
    }

    @Nullable
    private Collection<String> getVendorExtensions(Object object, String extensionName) throws ReverseAstException {
        if (object instanceof Map) {
            return getVendorExtensions((Map) object, extensionName);
        }
        for (Method m : object.getClass().getDeclaredMethods()) {
            if (extensionMethodNames.contains(m.getName())) {
                try {
                    Object extensions = m.invoke(object);
                    if (extensions instanceof Map) {
                        return getVendorExtensions((Map) extensions, extensionName);
                    }
                } catch (ReflectiveOperationException e) {
                    throw new ReverseAstException("Error getting extensions.", e);
                }
            }
        }
        return null;
    }

    @Nullable
    private Collection<String> getVendorExtensions(Map<?, ?> map, String extensionName) {
        if (map.containsKey(extensionName)) {
            Object value = ((Map) map).get(extensionName);
            if (value instanceof String) {
                return Collections.singleton((String) value);
            }
            if (value instanceof Collection) {
                return ((Collection<?>) value).stream().map(Object::toString).collect(Collectors.toSet());
            }
        }
        return null;
    }
}
