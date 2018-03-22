package de.zalando.zally.rule.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.swagger.models.Swagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

/**
 * CGLib proxy for filtering objects using <code>getVendorExtensions</code> method and provided predicate.
 */
public class SwaggerProxy {


    private static final Logger LOG = LoggerFactory.getLogger(SwaggerProxy.class);

    private static final String EXTENSIONS_METHOD_NAME = "getVendorExtensions";

    private final Function<Map<String, Object>, Boolean> predicate;


    public SwaggerProxy(Function<Map<String, Object>, Boolean> predicate) {
        this.predicate = predicate;
    }

    public Swagger build(Swagger original) {
        return (Swagger) buildProxy(original);
    }

    private Object buildProxy(Object original) {
        if (original == null) {
            return null;
        }
        if (Enhancer.isEnhanced(original.getClass())) {
            return original;
        }
        LOG.debug("Proxying {} instance", original.getClass().getName());
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(original.getClass());
        enhancer.setCallback(new Interceptor(original));
        return enhancer.create();
    }

    @SuppressWarnings("unchecked")
    private class Interceptor implements MethodInterceptor {

        private final Object original;

        private Interceptor(Object original) {
            this.original = original;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            Object result = method.invoke(original, args);
            LOG.debug("Checking result of \"{}\" method", method.getName());
            return filter(result).orElse(null);
        }


        private Optional<Object> filter(final Object obj) {
            if (obj == null) {
                return Optional.empty();
            }
            if (Map.class.isAssignableFrom(obj.getClass())) {
                return filterMap((Map<Object, Object>) obj);
            } else if (List.class.isAssignableFrom(obj.getClass())) {
                return filterCollection((Collection<Object>) obj, List.class);
            } else if (Set.class.isAssignableFrom(obj.getClass())) {
                return filterCollection((Collection<Object>) obj, Set.class);
            }

            Optional<Method> extensionsMethod = extensionsMethod(obj);
            if (!extensionsMethod.isPresent()) {
                return Optional.of(obj);
            }
            return extensionsMethod
                    .flatMap(m -> getExtensions(obj, m))
                    .flatMap(ext -> predicate.apply(ext) ? Optional.ofNullable(buildProxy(obj)) : Optional.empty());
        }


        private Optional<Object> filterCollection(Collection<Object> source, Class targetCollection) {
            if (source == null) {
                return Optional.empty();
            }
            return Optional.of(source.stream()
                    .map(this::filter)
                    .filter(Optional::isPresent)
                    .collect(Collectors.toCollection(() -> {
                        if (Set.class.isAssignableFrom(targetCollection)) {
                            return new HashSet();
                        }
                        return new ArrayList();
                    })));
        }

        private Optional<Object> filterMap(Map<Object, Object> object) {
            Map result = new HashMap();
            object.forEach((key, value) -> filter(value).ifPresent(v -> result.put(key, v)));
            return Optional.of(result);
        }

        private Optional<Map<String, Object>> getExtensions(Object obj, Method method) {
            try {
                Map<String, Object> result = (Map<String, Object>) method.invoke(obj);
                return Optional.ofNullable(result);
            } catch (Exception e) {
                LOG.error("Failed to filter value", e);
                return Optional.empty();
            }
        }

        private Optional<Method> extensionsMethod(Object obj) {
            try {
                return Optional.of(obj.getClass().getMethod(EXTENSIONS_METHOD_NAME));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

    }

}
