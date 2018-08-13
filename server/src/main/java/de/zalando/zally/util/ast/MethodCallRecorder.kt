package de.zalando.zally.util.ast

import com.fasterxml.jackson.core.JsonPointer
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.framework.ProxyFactory
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.Arrays
import java.util.IdentityHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.set

/**
 * MethodCallRecorder creates a Proxy around an object, typically Swagger or OpenApi, and records
 * successive method calls as a JSON pointer. Any returned property that is a complex object or
 * a container will be also wrapped in a Proxy. Null-values will be transformed into non-null
 * Proxies as well, making it possible to generate JSON pointers for all possible properties.
 */
class MethodCallRecorder<T>(`object`: T) {

    val proxy: T = createProxy(`object`, null)
    var pointer: JsonPointer = JsonPointers.empty()

    private val skipMethods = HashSet<String>()
    private val objectPointerCache = IdentityHashMap<Any, JsonPointer>()
    private val methodPointerCache = IdentityHashMap<Any, IdentityHashMap<Method, JsonPointer>>()

    internal class MethodCallRecorderException : Throwable {
        constructor(message: String) : super(message)

        constructor(message: String, cause: Throwable) : super(message, cause)
    }

    private fun isGetterMethod(m: Method): Boolean {
        return m.name.startsWith("get") && m.returnType != null
    }

    private fun isPrimitive(o: Any): Boolean {
        return isPrimitive(o.javaClass)
    }

    private fun isPrimitive(c: Class<*>): Boolean {
        return Util.PRIMITIVES.contains(c)
    }

    private fun isGenericContainer(o: Any): Boolean {
        return o is Collection<*> || o is Map<*, *>
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(MethodCallRecorderException::class)
    private fun <T> createInstance(c: Class<T>): T {
        return when {
            c.isAssignableFrom(Map::class.java) -> HashMap<Any, Any>() as T
            c.isAssignableFrom(List::class.java) -> ArrayList<Any>() as T
            c.isAssignableFrom(Set::class.java) -> HashSet<Any>() as T
            c.isArray -> arrayOfNulls<Any>(0) as T
            else -> try {
                c.getConstructor().newInstance()
            } catch (e: ReflectiveOperationException) {
                throw MethodCallRecorderException("Cannot create " + c.toString(), e)
            }
        }
    }

    @Throws(MethodCallRecorderException::class)
    private fun getGenericReturnValueType(m: Method): Class<*> {
        val type = m.genericReturnType
        if (type is ParameterizedType) {
            val typeArgs = type.actualTypeArguments
            return typeArgs[typeArgs.size - 1] as Class<*>
        }
        throw MethodCallRecorderException(m.returnType.toString())
    }

    fun skipMethods(vararg pointer: String): MethodCallRecorder<T> {
        this.skipMethods.addAll(Arrays.asList(*pointer))
        return this
    }

    @Suppress("UNCHECKED_CAST")
    private fun <U> createProxy(`object`: U, parent: Method?): U {
        val interceptor = createMethodInterceptor(`object` as Any, parent)
        val factory = ProxyFactory()
        factory.setTarget(`object`)
        factory.addAdvice(interceptor)
        return factory.proxy as U
    }

    private fun createMethodInterceptor(`object`: Any, parent: Method?): MethodInterceptor {
        return MethodCallInterceptor(`object`, parent)
    }

    private fun updatePointer(`object`: Any, method: Method, arguments: Array<Any>) {
        // Some methods should not be recorded in the JSON pointer string.
        if (skipMethods.contains(method.name)) {
            return
        }
        // In order to prevent multiple successive method calls the the proxied object to
        // endlessly append new fragments to the JSON pointer string, we must remember already
        // recorded pointers inside two caches:
        // objectPointerCache holds "base" pointers with the original objects on which methods are
        // called as keys.
        // methodPointerCache is a nested map that holds "method" pointers with the objects and the
        // called methods as keys.
        val objectPointer: JsonPointer?
        if (objectPointerCache.containsKey(`object`)) {
            objectPointer = objectPointerCache[`object`]
        } else {
            objectPointerCache[`object`] = pointer
            objectPointer = pointer
        }
        if (methodPointerCache.containsKey(`object`)) {
            val methodMap = methodPointerCache[`object`]!!
            if (methodMap.containsKey(method)) {
                pointer = methodMap[method]!!
            } else {
                pointer = objectPointer!!.append(JsonPointers.escape(method, *arguments))
                methodMap[method] = pointer
            }
        } else {
            val methodMap = IdentityHashMap<Method, JsonPointer>()
            pointer = objectPointer!!.append(JsonPointers.escape(method, *arguments))
            methodMap[method] = pointer
            methodPointerCache[`object`] = methodMap
        }
    }

    private inner class MethodCallInterceptor(private val `object`: Any, private val parent: Method?) : MethodInterceptor {

        @Throws(Throwable::class)
        override fun invoke(invocation: MethodInvocation): Any? {
            val m = invocation.method
            if (!isGetterMethod(m)) {
                return invocation.proceed()
            }

            val arguments = invocation.arguments
            updatePointer(`object`, m, arguments)
            val result = m.invoke(invocation.getThis(), *arguments)

            // The result is null but we must construct a Proxy of the result type anyway.
            if (result == null) {
                val returnType = m.returnType
                // Primitives are directly returned as null.
                if (isPrimitive(returnType)) {
                    return null
                }
                // If the object on which the method was called is a generic container we
                // need some special logic in order to detect the correct return type.
                if (isGenericContainer(`object`)) {
                    val genericReturnValueType = getGenericReturnValueType(parent!!)
                    // If the declared value type of the generic container is a plain object or a primitive,
                    // it does not make sense to create a new instance. We can simply return null instead.
                    return when {
                        genericReturnValueType == Any::class.java || isPrimitive(genericReturnValueType) -> null
                        else -> createProxy(createInstance(genericReturnValueType), m)
                    }
                }
                // For all other complex object types we can attempt to simply instantiate them.
                return createProxy(createInstance(returnType), m)
            }
            // Primitives are not wrapped in Proxies.
            return when {
                isPrimitive(result) -> result
                else -> createProxy(result, m)
            }
        }
    }
}
