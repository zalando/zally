package de.zalando.zally.util;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class OpenApiWalker {
  public static Map<Object, String> walk(Object object, Collection<Class<?>> ignore) {
    Queue<Object> objects = new LinkedList<>();
    Queue<String> pointers = new LinkedList<>();  //
    Map<Object, String> map = new IdentityHashMap<>();
    objects.add(object);
    pointers.add("#"); // JSON pointer root

    while (!objects.isEmpty()) {
      Object o = objects.remove();
      String path = pointers.remove();
      map.put(o, path);

      if (!ignore.contains(o.getClass())) {
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
              pointers.add(path + "/" + rfc6901Encode((String) key));
              objects.add(value);
            }
          }
        } else if (o instanceof List) {
          List<?> l = (List<?>) o;
          for (int i = 0; i < l.size(); i += 1) {
            Object value = l.get(i);
            if (value != null) {
              pointers.add(path + "/" + i);
              objects.add(value);
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
                    pointers.add(path);
                  } else {
                    pointers.add(path + "/" + getterNameToPathName(name));
                  }
                  objects.add(value);
                }
              } catch (ReflectiveOperationException e) {
                String message = String.format("Error invoking %s on %s at path %s", name, o.getClass(), path);
                throw new RuntimeException(message, e);
              }
            }
          }
        }
      }
    }
    return map;
  }

  // https://tools.ietf.org/html/rfc6901
  private static String rfc6901Encode(String s) {
    return s.replace("~", "~0").replace("/", "~1");
  }

  private static String getterNameToPathName(String name) {
    String s = name.replace("get", "");
    return s.substring(0, 1).toLowerCase() + s.substring(1);
  }
}
