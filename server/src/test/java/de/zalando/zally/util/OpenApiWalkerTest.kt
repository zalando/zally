package de.zalando.zally.util

import de.zalando.zally.util.ResourceUtil.resourceToString
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class OpenApiWalkerTest {
    @Test
    fun `walk Swagger object`() {
        val content = resourceToString("fixtures/api_spp.json")
        val obj = SwaggerParser().parse(content)
        val map = OpenApiWalker.walk(obj)
        assertThat(map).isNotEmpty
//        println(map.values.joinToString("\n"))
    }
}
