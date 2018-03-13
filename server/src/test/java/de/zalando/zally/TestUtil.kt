package de.zalando.zally

import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import de.zalando.zally.rule.CheckDetails
import de.zalando.zally.rule.ObjectTreeReader
import de.zalando.zally.rule.RulesPolicy
import de.zalando.zally.rule.SwaggerContext
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import de.zalando.zally.rule.api.Violation
import io.swagger.models.ModelImpl
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.HeaderParameter
import io.swagger.models.properties.StringProperty
import io.swagger.parser.SwaggerParser
import io.swagger.parser.util.ClasspathHelper
import kotlin.reflect.KFunction1
import kotlin.reflect.jvm.javaMethod

val testConfig: Config by lazy {
    ConfigFactory.load("rules-config.conf")
}

fun getFixture(fileName: String): Swagger = SwaggerParser().read("fixtures/$fileName")

fun getResourceContent(fileName: String): String = ClasspathHelper.loadFileFromClasspath("fixtures/$fileName")

fun getResourceJson(fileName: String): JsonNode = ObjectTreeReader().read(getResourceContent(fileName))

fun swaggerWithPaths(vararg specificPaths: String): Swagger =
    Swagger().apply {
        paths = specificPaths.map { it to Path() }.toMap()
    }

fun swaggerWithHeaderParams(vararg names: String) =
    Swagger().apply {
        parameters = names.map { header ->
            header to HeaderParameter().apply { name = header }
        }.toMap()
    }

fun swaggerWithDefinitions(vararg defs: Pair<String, List<String>>): Swagger =
        Swagger().apply {
            definitions = defs.map { def ->
                def.first to ModelImpl().apply {
                    properties = def.second.map { prop -> prop to StringProperty() }.toMap()
                }
            }.toMap()
        }

fun swaggerWithOperations(operations: Map<String, Iterable<String>>): Swagger =
        Swagger().apply {
            val path = Path()
            operations.forEach { method, statuses ->
                val operation = Operation().apply {
                    statuses.forEach { addResponse(it, Response()) }
                }
                path.set(method, operation)
            }
            paths = mapOf("/test" to path)
        }

/**
 * Build up a SwaggerContext and invoke the check method.
 * @param swagger the model
 * @param instance the rule instance
 * @param functionReference an instance reference to the check method
 * @param policy the policy to apply, defaulting to an empty policy
 * @return Violation as returned by the check method.
 */
@Suppress("UnsafeCast", "UnsafeCallOnNullableType")
fun validateSwaggerContext(
    swagger: Swagger,
    instance: Any,
    functionReference: KFunction1<@ParameterName(name = "context") SwaggerContext, Violation?>,
    policy: RulesPolicy = RulesPolicy(emptyArray())
): Violation? {
    val rule = instance.javaClass.getAnnotation(Rule::class.java)
    val method = functionReference.javaMethod!!
    val check = method.getAnnotation(Check::class.java)
    val ruleSetClass = rule.ruleSet.java
    val ruleSet = ruleSetClass.newInstance() as RuleSet
    val details = CheckDetails(ruleSet, rule, instance, check, method)
    val context = SwaggerContext(swagger, policy, details)
    return functionReference.invoke(context)
}
