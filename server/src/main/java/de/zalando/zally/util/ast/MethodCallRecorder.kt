package de.zalando.zally.util.ast

import com.fasterxml.jackson.core.JsonPointer
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.framework.ProxyFactory
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.IdentityHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * MethodCallRecorder creates a Proxy around an obj, typically Swagger or OpenApi, and records
 * successive method calls as a JSON pointer. Any returned property that is a complex obj or
 * a container will be also wrapped in a Proxy. Null-values will be transformed into non-null
 * Proxies as well, making it possible to generate JSON pointers for all possible properties.
 */
class MethodCallRecorder<T : Any>(obj: T) {

    val proxy: T = createProxy(obj)
    var pointer: JsonPointer = JsonPointers.EMPTY

    private val skipMethods = HashSet<String>()
    private val objectPointerCache = IdentityHashMap<Any, JsonPointer>()
    private val methodPointerCache = IdentityHashMap<Any, IdentityHashMap<Method, JsonPointer>>()

    internal class MethodCallRecorderException(message: String, cause: Throwable? = null) : Throwable(message, cause)

    private fun isGetterMethod(m: Method): Boolean = m.name.startsWith("get") && m.returnType != null

    private fun isPrimitive(o: Any): Boolean = isPrimitive(o.javaClass)

    private fun isPrimitive(c: Class<*>): Boolean = Util.PRIMITIVES.contains(c)

    private fun isGenericContainer(o: Any): Boolean = o is Collection<*> || o is Map<*, *>

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
    private fun getGenericReturnValueType(m: Method): Class<*> =
        (m.genericReturnType as? ParameterizedType)
        ?.let { it.actualTypeArguments.last() as Class<*> }
        ?: throw MethodCallRecorderException(m.returnType.toString())

    fun skipMethods(vararg methodNames: String): MethodCallRecorder<T> {
        this.skipMethods += listOf(*methodNames)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    private fun <U : Any> createProxy(obj: U, parent: Method? = null): U {
        val interceptor = createMethodInterceptor(obj, parent)
        val factory = ProxyFactory()
        factory.setTarget(obj)
        factory.addAdvice(interceptor)
        return factory.proxy as U
    }

    private fun createMethodInterceptor(obj: Any, parent: Method?): MethodInterceptor {
        return MethodCallInterceptor(obj, parent)
    }

    private fun updatePointer(obj: Any, method: Method, arguments: Array<Any>) {
        // Some methods should not be recorded in the JSON pointer string.
        if (method.name in skipMethods) {
            return
        }
        // In order to prevent multiple successive method calls the the proxied obj to
        // endlessly append new fragments to the JSON pointer string, we must remember already
        // recorded pointers inside two caches:
        // objectPointerCache holds "base" pointers with the original objects on which methods are
        // called as keys.
        // methodPointerCache is a nested map that holds "method" pointers with the objects and the
        // called methods as keys.
        val objectPointer: JsonPointer? = objectPointerCache.getOrPut(obj) { pointer }
        val methodMap = methodPointerCache.getOrPut(obj) { IdentityHashMap() }
        pointer = methodMap.getOrPut(method) { objectPointer!!.append(JsonPointers.escape(method, *arguments)) }
    }

    private inner class MethodCallInterceptor(private val obj: Any, private val parent: Method?) : MethodInterceptor {

        @Throws(Throwable::class)
        override fun invoke(invocation: MethodInvocation): Any? {
            val m = invocation.method
            if (!isGetterMethod(m)) {
                return invocation.proceed()
            }

            val arguments = invocation.arguments
            updatePointer(obj, m, arguments)
            val result = m.invoke(invocation.getThis(), *arguments)

            // The result is null but we must construct a Proxy of the result type anyway.
            if (result == null) {
                val returnType = m.returnType
                // Primitives are directly returned as null.
                if (isPrimitive(returnType)) {
                    return null
                }
                // If the obj on which the method was called is a generic container we
                // need some special logic in order to detect the correct return type.
                if (isGenericContainer(obj)) {
                    val genericReturnValueType = getGenericReturnValueType(parent!!)
                    // If the declared value type of the generic container is a plain obj or a primitive,
                    // it does not make sense to create a new instance. We can simply return null instead.
                    return when {
                        genericReturnValueType == Any::class.java || isPrimitive(genericReturnValueType) -> null
                        else -> createProxy(createInstance(genericReturnValueType), m)
                    }
                }
                // For all other complex obj types we can attempt to simply instantiate them.
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
