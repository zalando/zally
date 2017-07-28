package de.zalando.zally.rule

import de.zalando.zally.swaggerWithPaths
import de.zalando.zally.testSpecPointerProvider
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AvoidTrailingSlashesRuleTest {

    @Test
    fun emptySwagger() {
        assertThat(AvoidTrailingSlashesRule(testSpecPointerProvider).validate(Swagger())).isNull()
    }

    @Test
    fun positiveCase() {
        val testAPI = swaggerWithPaths("/api/test-api")
        assertThat(AvoidTrailingSlashesRule(testSpecPointerProvider).validate(testAPI)).isNull()
    }

    @Test
    fun negativeCase() {
        val testAPI = swaggerWithPaths("/api/test-api/", "/api/test", "/some/other/path", "/long/bad/path/with/slash/")
        val violations = AvoidTrailingSlashesRule(testSpecPointerProvider).validate(testAPI)!!
        assertThat(violations.paths).hasSameElementsAs(listOf("/api/test-api/", "/long/bad/path/with/slash/"))
        assertThat(violations.specPointers).hasSameElementsAs(listOf("/paths/~1api~1test-api~1", "/paths/~1long~1bad~1path~1with~1slash~1"))
    }
}
