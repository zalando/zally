package de.zalando.zally.rule

import de.zalando.zally.rule.api.Context
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class DefaultContextTest {

    //
    // OPEN API
    //

    @Test
    fun `OPEN API -- not applicable when content does not contain the openapi property`() {
        @Language("YAML")
        val content = """
                some: properties
                but: no
                property: called OpenAPI
            """
        val result = DefaultContext.createOpenApiContext(content)
        assertThat(result).resultsInNotApplicable()
    }

    @Test
    fun `OPEN API -- openapi specification must contain info and paths`() {
        // The parsing results in a valid OpenAPI 3 object model, but
        // with messages that `info` and `paths` are missing. Let the
        // rules check that out.
        @Language("YAML")
        val content = """
                openapi: 3.0.0
            """
        val result = DefaultContext.createOpenApiContext(content)
        assertThat(result).resultsInErrors(
            "attribute info is missing",
            "attribute paths is missing"
        )
    }

    @Test
    fun `OPEN API -- oauth without scopes succeeds`() {
        @Language("YAML")
        val content = """
                openapi: 3.0.0
                info:
                  title: Foo
                  version: 1.0.0
                security:
                  - type: oauth2
                    flow: implicit
                    authorizationUrl: https://identity.some-server/auth
                paths: {}
            """
        val result = DefaultContext.createOpenApiContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `OPEN API -- does not recognize a Swagger file`() {
        @Language("YAML")
        val content = """
                swagger: '2.0'
                info:
                  version: 1.0.0
                  title: Pets API
                paths: {}
            """.trimIndent()
        val result = DefaultContext.createOpenApiContext(content)
        assertThat(result).resultsInNotApplicable()
    }

    //
    // SWAGGER
    //

    @Test
    fun `SWAGGER -- not applicable when content does not contain the swagger property`() {
        @Language("YAML")
        val content = """
                some: properties
                but: no
                property: called OpenAPI
            """
        val result = DefaultContext.createSwaggerContext(content)
        assertThat(result).resultsInNotApplicable()
    }

    companion object {

        class ContentParseResultAssert(actual: ContentParseResult<Context>?)
            : AbstractAssert<ContentParseResultAssert, ContentParseResult<Context>?>(actual, ContentParseResultAssert::class.java) {

            fun resultsInSuccess() {
                this.isInstanceOf(ContentParseResult.Success::class.java)
            }

            fun resultsInNotApplicable() {
                this.isInstanceOf(ContentParseResult.NotApplicable::class.java)
            }

            fun resultsInErrors(vararg expectedErrors: String) {
                isInstanceOf(ContentParseResult.ParsedWithErrors::class.java)
                val result = actual as ContentParseResult.ParsedWithErrors<Context>
                assertThat(result.errors).hasSameElementsAs(expectedErrors.toList())
            }

        }

        fun assertThat(actual: ContentParseResult<Context>) = ContentParseResultAssert(actual)
    }
}

//class DefaultContextTest {
//    @Test
//    fun createSwaggerContextFromSwaggerJson() {
//        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
//        val context = DefaultContext.createSwaggerContext(content)
//        assertThat(context).isNotNull
//        assertThat(context?.api).isInstanceOf(OpenAPI::class.java)
//        assertThat(context?.violation("", context.api.info.title)?.pointer).hasToString("/info/title")
//    }
//
//    @Test
//    fun createOpenApiContextFromSwaggerJson() {
//        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
//        val context = DefaultContext.createOpenApiContext(content)
//        assertThat(context).isNull()
//    }
//
//    @Test
//    fun createOpenApiContextFromOpenApiJson() {
//        val content = resourceToString("fixtures/openapi3_petstore_expanded.json")
//        val context = DefaultContext.createOpenApiContext(content)
//        assertThat(context).isNotNull
//        assertThat(context?.api).isInstanceOf(OpenAPI::class.java)
//        assertThat(context?.violation("", context.api.info.title)?.pointer).hasToString("/info/title")
//    }
//
//    @Test
//    fun `should recognize the used OpenAPI 2 (aka Swagger)`() {
//        val openapi3Context = DefaultContext.createSwaggerContext("""
//        swagger: '2.0'
//        info:
//          version: 1.0.0
//          title: Pets API
//        paths: {}
//        """.trimIndent())!!
//
//        assertThat(openapi3Context.isOpenAPI3()).isFalse()
//    }
//
//    @Test
//    fun `should recognize the used OpenAPI 3`() {
//        val openapi3Context = DefaultContext.createOpenApiContext("""
//        openapi: 3.0.1
//        info:
//          title: Pets API
//          version: 1.0.0
//        paths: {}
//        """.trimIndent())!!
//
//        assertThat(openapi3Context.isOpenAPI3()).isTrue()
//    }
//
//    class `Create and Pre Checks` {
//
//        class Swagger {
//
//            @Test
//            fun `YAML without swagger property is not consider a Swagger spec`() {
//                TODO()
//                @Language("YAML")
//                val content = """
//                truc: muche
//            """.trimIndent()
//                val violations = assertHasPreCheckViolations(content)
//                assertThat(violations).contains(
//                    Violation("", JsonPointer.compile("/"))
//                )
//
//            }
//
//        }
//
//        class OpenAPI {
//            @Test
//            fun `YAML without openapi property is not consider an OpenAPI spec`() {
//                TODO()
//                @Language("YAML")
//                val content = """
//                truc: muche
//            """.trimIndent()
//                val violations = assertHasPreCheckViolations(content)
//                assertThat(violations).contains(
//                    Violation("", JsonPointer.compile("/"))
//                )
//
//            }
//        }
//
//        @Test
//        fun `spec without INFO causes pre-check violations`() {
//            // Specific case where converting from Swagger to OpenAPI 3 (using the `Context`
//            // object) would throw an exception. New behaviour tested here: the returned `Context`
//            // is null because the file was not parsed (convertible, here).
//            @Language("YAML")
//            val content = """
//                truc: muche
//            """.trimIndent()
//            val violations = assertHasPreCheckViolations(content)
//            assertThat(violations).contains(
//                Violation("", JsonPointer.compile("/"))
//            )
//
//        }
//
//        @Test
//        fun `spec without OAUTH scopes should not crash`() {
//            // Specific case where converting from Swagger to OpenAPI 3 (using the `Context`
//            // object) would throw an exception. New behaviour tested here: the returned `Context`
//            // is null because the file was not parsed (convertible, here).
//            @Language("YAML")
//            val content = """
//                swagger: 2.0
//                info:
//                  title: Bleh
//                securityDefinitions:
//                  oa:
//                    type: oauth2
//                    flow: application
//                    # scopes:
//                    #   foo: Description of 'foo'
//                paths: {}
//            """.trimIndent()
//            val violations = assertHasPreCheckViolations(content)
//            assertThat(violations).hasSameElementsAs(listOf(
//                TODO()
//            ))
//
//        }
//
//        companion object {
//
//            private fun assertHasPreCheckViolations(content: String): List<Violation> {
//                val thrown = catchThrowable { DefaultContext.createSwaggerContext(content, true) }
//                assertThat(thrown).isInstanceOf(PreCheckViolationsException::class.java)
//                return (thrown as PreCheckViolationsException).violations
//            }
//        }
//    }
//}
