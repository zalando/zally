package de.zalando.zally.util

import de.zalando.zally.util.ResourceUtil.resourceToString
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class OpenApiWalkerTest {
    @Test
    fun `walk Swagger object`() {
        val content = resourceToString("fixtures/api_spp.json")
        val obj = SwaggerParser().parse(content)
        val ignore = HashSet(Arrays.asList(
                io.swagger.models.ArrayModel::class.java,
                io.swagger.models.ComposedModel::class.java,
                io.swagger.models.ModelImpl::class.java
        ))
        val map = OpenApiWalker.walk(obj, ignore as Collection<Class<*>>?)
        assertThat(map).isNotEmpty
        println(map.values.joinToString("\n"))
        println("mapped ${map.size} paths")
    }
}
