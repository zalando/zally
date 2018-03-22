package de.zalando.zally.util;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class OpenApiWalker {
  private OpenApiWalker() {
  }

  public static class OpenApiWalkerResult {
    private final Map<Object, String> pointers;

    private OpenApiWalkerResult(Map<Object, String> pointers) {
      this.pointers = pointers;
    }

    public Map<Object, String> getPointers() {
      return pointers;
    }
  }

  private static enum Marker {
    X_ZALLY_IGNORE("x-zally-ignore");

    final String key;

    Marker(String key) {
      this.key = key;
    }
  }

  public static OpenApiWalkerResult walk(Object object, Collection<Class<?>> ignore) {
    Deque<Object> objects = new LinkedList<>();
    Deque<String> pointers = new LinkedList<>();
    Map<Object, String> pointersMap = new IdentityHashMap<>();
    objects.push(object);
    pointers.push("#"); // JSON pointer root

    while (!objects.isEmpty()) {
      Object o = objects.pop(); // .remove();
      String path = pointers.pop(); // .remove();
      pointersMap.put(o, path);

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
            pointers.push(path.concat("/").concat(rfc6901Encode((String) key)));
            objects.push(value);
          }
        }
      } else if (o instanceof List) {
        List<?> l = (List<?>) o;
        for (int i = 0; i < l.size(); i += 1) {
          Object value = l.get(i);
          if (value != null) {
            pointers.push(path + "/" + i);
            objects.push(value);
          }
        }
      } else {
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
                  // We must not use the method name but re-use the current path.
                  pointers.push(path);
                } else {
                  pointers.push(path.concat("/").concat(getterNameToPathName(name)));
                }
                objects.push(value);
              }
            } catch (ReflectiveOperationException e) {
              String message = String.format("Error invoking %s on %s at path %s", name, o.getClass(), path);
              throw new RuntimeException(message, e);
            }
          }
        }
      }
    }
    return new OpenApiWalkerResult(pointersMap);
  }

  // https://tools.ietf.org/html/rfc6901
  private static String rfc6901Encode(String s) {
    return s.replace("~", "~0").replace("/", "~1");
  }

  private static String getterNameToPathName(String name) {
    String s = name.substring(3); // `get` is first 3 characters
    return s.substring(0, 1).toLowerCase().concat(s.substring(1));
  }
}
