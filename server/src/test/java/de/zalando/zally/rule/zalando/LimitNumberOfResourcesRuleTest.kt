package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LimitNumberOfResourcesRuleTest {

    private val rule = LimitNumberOfResourcesRule(testConfig)

    @Test
    fun positiveCase() {
        val swagger = getFixture("limitNumberOfResourcesValid.json")
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }

    @Test
    fun negativeCase() {
        val swagger = getFixture("limitNumberOfResourcesInvalid.json")
        val result = rule.validate(ApiAdapter(swagger))!!
        assertThat(result.paths).hasSameElementsAs(listOf(
            "/items",
            "/items10",
            "/items3",
            "/items4",
            "/items5",
            "/items6",
            "/items7",
            "/items8",
            "/items9"))
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
            "/customers/{id}/addresses/{addr}")

        val resourceTypes = rule.resourceTypes(paths)
        assertThat(resourceTypes).hasSameElementsAs(listOf(
                "/",
                "/customers",
                "/addresses",
                "/customers/{id}/addresses"))
    }

    @Test
    fun resourceTypeWithRootPathReturnsRoot() {
        val resourceType = rule.resourceType("/")
        assertThat(resourceType).isEqualTo("/")
    }

    @Test
    fun resourceTypeWithOneComponentPathReturnsPathNormalized() {
        val resourceType = rule.resourceType("/one")
        assertThat(resourceType).isEqualTo("/one")
    }

    @Test
    fun resourceTypeWithFixedComponentsPathReturnsPathNormalized() {
        val resourceType = rule.resourceType("one///two/three/four/five/six/")
        assertThat(resourceType).isEqualTo("/one/two/three/four/five/six")
    }

    @Test
    fun resourceTypeWithTrailingParameterPathReturnsPrefix() {
        val resourceType = rule.resourceType("/one/two/three/{item}/")
        assertThat(resourceType).isEqualTo("/one/two/three")
    }

    @Test
    fun resourceTypeWithPenultimateParameterPathReturnsPrefix() {
        val resourceType = rule.resourceType("/one/two/{item}/two")
        assertThat(resourceType).isEqualTo("/one/two")
    }
}
