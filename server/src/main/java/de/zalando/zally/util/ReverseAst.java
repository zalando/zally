package de.zalando.zally.util;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * ReverseAst holds meta information for nodes of a Swagger or OpenApi object.
 */
public class ReverseAst {
  private static Collection<String> EXTENSION_METHOD_NAMES = new HashSet<>(Arrays.asList(
      "getVendorExtensions",
      "getExtensions"
  ));

  /**
   * Meta information for an object node in a Swagger or OpenApi object.
   */
  private static class Meta {
    private final String pointer;
    private final Marker marker;
    private final String markerValue;

    private Meta(String pointer, Node marker) {
      this.pointer = pointer;
      this.marker = marker != null ? marker.marker : null;
      this.markerValue = marker != null ? marker.pointer : null;
    }
  }

  /**
   * A stack node for tree-traversal.
   */
  private static class Node {
    private final Object object;
    private final String pointer;
    private final boolean skip;
    private final Marker marker;

    Node(Object object, String pointer) {
      this(object, pointer, false, null);
    }

    Node(Object object, String pointer, boolean skip) {
      this(object, pointer, skip, null);
    }

    Node(Object object, String pointer, Marker marker) {
      this(object, pointer, false, marker);
    }

    Node(Object object, String pointer, boolean skip, Marker marker) {
      this.object = object;
      this.pointer = pointer;
      this.skip = skip;
      this.marker = marker;
    }

    boolean shouldSkip() {
      return this.skip;
    }

    boolean isMarker() {
      return this.marker != null;
    }
  }

  /**
   * A type of marker for an object node in a Swagger or OpenApi structure.
   */
  public enum Marker {
    X_ZALLY_IGNORE("x-zally-ignore");

    final String key;

    Marker(String key) {
      this.key = key;
    }
  }

  public static class ReverseAstException extends Exception {
    ReverseAstException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Creates a new instance of ReverseAst. Traverses a Swagger or OpenApi object tree
   * and constructs a map of object nodes to meta information objects.
   *
   * @param object Swagger or OpenApi instance.
   * @param ignore List of classes in the traversable object that should be ignored.
   * @return ReverseAst instance.
   * @throws ReverseAstException If an error occurs during reflection.
   */
  public static ReverseAst create(Object object, Collection<Class<?>> ignore) throws ReverseAstException {
    Deque<Node> nodes = new LinkedList<>(); // stack of tree nodes
    Deque<Node> markers = new LinkedList<>(); // stack of markers for sub-trees
    Map<Object, Meta> map = new IdentityHashMap<>(); // map of node objects to JSON pointers
    nodes.push(new Node(object, "#")); // add the root node

    while (!nodes.isEmpty()) {
      Node node = nodes.pop(); // pop the current node
      Node marker = markers.peek(); // set the current marker

      if (node.isMarker()) {
        marker = null; // clear the marker
        markers.pop(); // remove the marker
        if (nodes.isEmpty()) {
          continue;
        } else {
          node = nodes.pop(); // get the next node
        }
      }

      final Object o = node.object;
      final String p = node.pointer;

      // Add the current node.
      if (!node.shouldSkip()) {
        map.put(o, new Meta(p, marker));
      }

      if (ignore.contains(o.getClass())) {
        continue;
      }
      if (o instanceof String) {
        continue;
      } else if (o instanceof Integer) {
        continue;
      } else if (o instanceof Float) {
        continue;
      } else if (o instanceof Double) {
        continue;
      } else if (o instanceof Boolean) {
        continue;
      } else if (o instanceof Enum) {
        continue;
      } else if (o instanceof Map) {
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) o).entrySet()) {
          Object key = entry.getKey();
          Object value = entry.getValue();
          if (key instanceof String && value != null) {
            String pointer = p.concat("/").concat(rfc6901Encode((String) key));
            nodes.push(new Node(value, pointer));
          }
        }
      } else if (o instanceof List) {
        List<?> l = (List<?>) o;
        for (int i = 0; i < l.size(); i += 1) {
          Object value = l.get(i);
          if (value != null) {
            String pointer = p.concat("/").concat(String.valueOf(i));
            nodes.push(new Node(value, pointer));
          }
        }
      } else {
        // Check if an ignore extension exists at this position.
        String ignoreExtension = getVendorExtension(o, Marker.X_ZALLY_IGNORE.key);
        if (ignoreExtension != null) {
          marker = new Node(ignoreExtension, ignoreExtension, Marker.X_ZALLY_IGNORE); // Set the current marker.
          markers.push(marker); // Add the marker to the marker stack.
          nodes.push(marker); // Use a node to mark the position on the node stack.
        }
        for (Method m : o.getClass().getDeclaredMethods()) {
          String name = m.getName();
          // Find all public getter methods.
          if (name.startsWith("get")
              && m.getParameterCount() == 0
              && Modifier.isPublic(m.getModifiers())
              && !m.isAnnotationPresent(JsonIgnore.class)) {
            try {
              Object value = m.invoke(o);
              if (value != null) {
                if (m.isAnnotationPresent(JsonAnyGetter.class)) {
                  // A `JsonAnyGetter` method is simply a wrapper for nested properties.
                  // We must not use the method name but re-use the current pointer.
                  nodes.push(new Node(value, p, /* skip */true));
                } else {
                  String pointer = p.concat("/").concat(getterNameToPathName(name));
                  nodes.push(new Node(value, pointer));
                }
              }
            } catch (ReflectiveOperationException e) {
              String message = String.format("Error invoking %s on %s at path %s", name, o.getClass(), p);
              throw new ReverseAstException(message, e);
            }
          }
        }
      }
    }
    return new ReverseAst(map);
  }

  @NotNull
  private static String rfc6901Encode(String s) {
    // https://tools.ietf.org/html/rfc6901
    return s.replace("~", "~0").replace("/", "~1");
  }

  @NotNull
  private static String getterNameToPathName(String name) {
    String s = name.substring(3); // `get` is first 3 characters
    return s.substring(0, 1).toLowerCase().concat(s.substring(1));
  }

  @Nullable
  private static String getVendorExtension(Object object, String extension) {
    for (Method m : object.getClass().getDeclaredMethods()) {
      if (EXTENSION_METHOD_NAMES.contains(m.getName())) {
        try {
          Object extensions = m.invoke(object);
          if (extensions instanceof Map) {
            return (String) ((Map) extensions).get(extension);
          }
        } catch (ReflectiveOperationException e) {
          return null;
        }
      }
    }
    return null;
  }

  private final Map<Object, Meta> map;

  private ReverseAst(Map<Object, Meta> map) {
    this.map = map;
  }

  @Nullable
  public String getPointer(Object key) {
    Meta meta = this.map.get(key);
    if (meta != null) {
      return meta.pointer;
    }
    return null;
  }

  public boolean isIgnored(Object key) {
    Meta meta = this.map.get(key);
    return meta != null && Marker.X_ZALLY_IGNORE.equals(meta.marker);
  }

  @Nullable
  public String getIgnoreValue(Object key) {
    Meta meta = this.map.get(key);
    if (meta != null && Marker.X_ZALLY_IGNORE.equals(meta.marker)) {
      return meta.markerValue;
    }
    return null;
  }
}
