package de.zalando.zally.core.ast

import de.zalando.zally.core.ast.ReverseAstBuilder.Companion.traversalMethods
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.parameters.QueryParameter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "StringLiteralDuplication")
class ReverseAstBuilderTest {

    @Test
    fun `traversalMethods with Enum skips getClass and getDeclaringClass`() {

        val methods = traversalMethods(PathItem.HttpMethod::class.java).map { it.name }

        assertThat(methods).isEmpty()
    }

    @Test
    fun `traversalMethods with Info returns declared getters only`() {

        val methods = traversalMethods(Info::class.java).map { it.name }

        assertThat(methods).containsExactly(
            "getContact",
            "getDescription",
            "getExtensions",
            "getLicense",
            "getTermsOfService",
            "getTitle",
            "getVersion"
        )
    }

    @Test
    fun `traversalMethods with QueryParameter returns inherited getters too`() {

        val methods = traversalMethods(QueryParameter::class.java).map { it.name }

        assertThat(methods).containsExactly(
            "get${'$'}ref",
            "getAllowEmptyValue",
            "getAllowReserved",
            "getContent",
            "getDeprecated",
            "getDescription",
            "getExample",
            "getExamples",
            "getExplode",
            "getExtensions",
            "getIn",
            "getName",
            "getRequired",
            "getSchema",
            "getStyle"
        )
    }

    @Test
    fun `traversalMethods with Swagger returns getPaths last`() {

        val methods = traversalMethods(Swagger::class.java).map { it.name }

        assertThat(methods).containsExactly(
            "getBasePath",
            "getConsumes",
            "getDefinitions",
            "getExternalDocs",
            "getHost",
            "getInfo",
            "getParameters",
            "getProduces",
            "getResponses",
            "getSchemes",
            "getSecurity",
            "getSecurityDefinitions",
            "getSwagger",
            "getTags",
            "getVendorExtensions",
            "getPaths"
        )
    }

    @Test
    fun `traversalMethods with OpenAPI returns leaves returns getPaths last`() {

        val methods = traversalMethods(OpenAPI::class.java).map { it.name }

        assertThat(methods).containsExactly(
            "getComponents",
            "getExtensions",
            "getExternalDocs",
            "getInfo",
            "getOpenapi",
            "getSecurity",
            "getServers",
            "getTags",
            "getPaths"
        )
    }
}
