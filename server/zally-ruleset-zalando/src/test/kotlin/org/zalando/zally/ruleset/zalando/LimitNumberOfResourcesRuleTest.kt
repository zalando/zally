package org.zalando.zally.ruleset.zalando

import com.typesafe.config.ConfigFactory
import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class LimitNumberOfResourcesRuleTest {

    private val config = ConfigFactory.parseString("""
        LimitNumberOfResourcesRule {
          resource_types_limit: 8
          path_whitelist: [
            "/whitelisted.*"
          ]
        }
        """.trimIndent())

    private val rule = LimitNumberOfResourcesRule(config)

    @Test
    fun `checkLimitOfResources should return a violation if the limit is reached`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              /resource1: {}
              /resource2: {}
              /resource3: {}
              /resource4: {}
              /resource5: {}
              /resource5/{resource_5_id}: {}
              /resource5/{resource_5_id}/resource6: {}
              /resource5/{resource_5_id}/resource6/{resource_6_id}: {}
              /resource7: {}
              /resource8: {}
              /resource9: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkLimitOfResources(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).containsPattern(".*greater than recommended limit of.*")
        assertThat(violation.pointer.toString()).isEqualTo("/paths")
    }

    @Test
    fun `checkLimitOfResources should return no violations if paths are whitelisted`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              /whitelisted1: {}
              /whitelisted2: {}
              /whitelisted3: {}
              /whitelisted4: {}
              /whitelisted5: {}
              /whitelisted5/{whitelisted_5_id}: {}
              /whitelisted5/{whitelisted_5_id}/whitelisted6: {}
              /whitelisted5/{whitelisted_5_id}/whitelisted6/{whitelisted_6_id}: {}
              /whitelisted7: {}
              /whitelisted8: {}
              /whitelisted9: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkLimitOfResources(context)

        assertThat(violation).isNull()
    }

    @Test
    fun `checkLimitOfResources should no return violation in case the number is under the limit`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              /resource: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violation = rule.checkLimitOfResources(context)

        assertThat(violation).isNull()
    }

    @Test
    fun resourceTypesWithCustomersAndAddressesExampleReturns3ResourceTypes() {
        val paths = listOf(
            "/",
            "/{id}",
            "/customers",
            "/customers/{id}",
            "/customers/{id}/preferences",
            "/addresses",
            "/addresses/{id}",
            "/customers/{id}/addresses",
            "/customers/{id}/addresses/{addr}"
        )

        val resourceTypes = rule.resourceTypes(paths)
        assertThat(resourceTypes).hasSameElementsAs(
            listOf(
                "/",
                "/customers",
                "/addresses",
                "/customers/{id}/addresses"
            )
        )
    }

    @Test
    fun `resourceType with root path should return root`() {
        val resourceType = rule.resourceType("/")
        assertThat(resourceType).isEqualTo("/")
    }

    @Test
    fun `resourceType with one component path should return normalized path`() {
        val resourceType = rule.resourceType("/one")
        assertThat(resourceType).isEqualTo("/one")
    }

    @Test
    fun `resourceType with fixed components path should return normalized path`() {
        val resourceType = rule.resourceType("one///two/three/four/five/six/")
        assertThat(resourceType).isEqualTo("/one/two/three/four/five/six")
    }

    @Test
    fun `resourceType with trailing parameter should return prefix`() {
        val resourceType = rule.resourceType("/one/two/three/{item}/")
        assertThat(resourceType).isEqualTo("/one/two/three")
    }

    @Test
    fun resourceTypeWithPenultimateParameterPathReturnsPrefix() {
        val resourceType = rule.resourceType("/one/two/{item}/two")
        assertThat(resourceType).isEqualTo("/one/two")
    }
}
