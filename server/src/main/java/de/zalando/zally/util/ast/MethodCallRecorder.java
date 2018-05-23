package de.zalando.zally.util.ast;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static de.zalando.zally.util.ast.Util.PRIMITIVES;
import static de.zalando.zally.util.ast.Util.getterNameToPointer;
import static de.zalando.zally.util.ast.Util.rfc6901Encode;

/**
 * MethodCallRecorder creates a Proxy around an object, typically Swagger or OpenApi, and records
 * successive method calls as a JSON pointer. Any returned property that is a complex object or
 * a container will be also wrapped in a Proxy. Null-values will be transformed into non-null
 * Proxies as well, making it possible to generate JSON pointers for all possible properties.
 */
public final class MethodCallRecorder<T> {
    static class MethodCallRecorderException extends Throwable {
        MethodCallRecorderException(String message) {
            super(message);
        }

        MethodCallRecorderException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static boolean isGetterMethod(Method m) {
        return m.getName().startsWith("get") && m.getReturnType() != null;
    }

    static boolean isPrimitive(Object o) {
        return isPrimitive(o.getClass());
    }

    static boolean isPrimitive(Class<?> c) {
        return PRIMITIVES.contains(c);
    }

    static boolean isGenericContainer(Object o) {
        return o instanceof Collection || o instanceof Map;
    }

    @SuppressWarnings("unchecked")
    static <T> T createInstance(Class<T> c) throws MethodCallRecorderException {
        if (c.isAssignableFrom(Map.class)) {
            return (T) new HashMap<>();
        }
        if (c.isAssignableFrom(List.class)) {
            return (T) new ArrayList<>();
        }
        if (c.isAssignableFrom(Set.class)) {
            return (T) new HashSet<>();
        }
        if (c.isArray()) {
            return (T) new Object[0];
        }
        try {
            Constructor<T> constructor = c.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new MethodCallRecorderException("Cannot create " + c.toString(), e);
        }
    }

    static Class<?> getGenericReturnValueType(Method m) throws MethodCallRecorderException {
        Type type = m.getGenericReturnType();
        if (type instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
            return (Class<?>) typeArgs[typeArgs.length - 1];
        }
        throw new MethodCallRecorderException(m.getReturnType().toString());
    }

    static String toPointer(String s) {
        return "/".concat(rfc6901Encode(getterNameToPointer(s)));
    }

    static String toPointer(Method m, Object... arguments) {
        String s = m.getName();
        if (arguments.length > 0) {
            s = s.concat(Objects.toString(arguments[0]));
        }
        return toPointer(s);
    }

    private final T proxy;
    private String currentPointer;
    private final Set<String> skipMethods = new HashSet<>();
    private final Map<Object, String> objectPointerCache = new IdentityHashMap<>();
    private final Map<Object, IdentityHashMap<Method, String>> methodPointerCache = new IdentityHashMap<>();

    public MethodCallRecorder(T object) {
        this.currentPointer = "#";
        this.proxy = createProxy(object, null);
    }

    @Nonnull
    public MethodCallRecorder<T> skipMethods(String... pointer) {
        this.skipMethods.addAll(Arrays.asList(pointer));
        return this;
    }

    @Nonnull
    public T getProxy() {
        return this.proxy;
    }

    @SuppressWarnings("unchecked")
    private <U> U createProxy(U object, Method parent) {
        MethodInterceptor interceptor = createMethodInterceptor(object, parent);
        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(object);
        factory.addAdvice(interceptor);
        return (U) factory.getProxy();
    }

    private MethodInterceptor createMethodInterceptor(Object object, Method parent) {
        return invocation -> {
            Method m = invocation.getMethod();
            Object[] arguments = invocation.getArguments();
            updatePointer(object, m, arguments);

            if (!isGetterMethod(m)) {
                return invocation.proceed();
            }
            Object result = m.invoke(invocation.getThis(), arguments);

            // The result is null but we must construct a Proxy of the result type anyway.
            if (result == null) {
                Class<?> returnType = m.getReturnType();
                // Primitives are directly returned as null.
                if (isPrimitive(returnType)) {
                    return null;
                }
                // If the object on which the method was called is a generic container we
                // need some special logic in order to detect the correct return type.
                if (isGenericContainer(object)) {
                    Class<?> genericReturnValueType = getGenericReturnValueType(parent);
                    // If the declared value type of the generic container is a plain object or a primitive,
                    // it does not make sense to create a new instance. We can simply return null instead.
                    if (genericReturnValueType.equals(Object.class) || isPrimitive(genericReturnValueType)) {
                        return null;
                    }
                    return createProxy(createInstance(genericReturnValueType), m);
                }
                // For all other complex object types we can attempt to simply instantiate them.
                return createProxy(createInstance(returnType), m);
            }
            // Primitives are not wrapped in Proxies.
            if (isPrimitive(result)) {
                return result;
            }
            return createProxy(result, m);
        };
    }

    private void updatePointer(Object object, Method method, Object[] arguments) {
        // Some methods should not be recorded in the JSON pointer string.
        if (skipMethods.contains(method.getName())) {
            return;
        }
        // In order to prevent multiple successive method calls the the proxied object to
        // endlessly append new fragments to the JSON pointer string, we must remember already
        // recorded pointers inside two caches:
        // objectPointerCache holds "base" pointers with the original objects on which methods are
        // called as keys.
        // methodPointerCache is a nested map that holds "method" pointers with the objects and the
        // called methods as keys.
        final String objectPointer;
        if (objectPointerCache.containsKey(object)) {
            objectPointer = objectPointerCache.get(object);
        } else {
            objectPointerCache.put(object, currentPointer);
            objectPointer = currentPointer;
        }
        if (methodPointerCache.containsKey(object)) {
            Map<Method, String> methodMap = methodPointerCache.get(object);
            if (methodMap.containsKey(method)) {
                currentPointer = methodMap.get(method);
            } else {
                currentPointer = objectPointer.concat(toPointer(method, arguments));
                methodMap.put(method, currentPointer);
            }
        } else {
            IdentityHashMap<Method, String> methodMap = new IdentityHashMap<>();
            currentPointer = objectPointer.concat(toPointer(method, arguments));
            methodMap.put(method, currentPointer);
            methodPointerCache.put(object, methodMap);
        }
    }

    @Nonnull
    public String getPointer() {
        return this.currentPointer;
    }
}
