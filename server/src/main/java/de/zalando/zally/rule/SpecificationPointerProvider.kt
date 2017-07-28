package de.zalando.zally.rule

import org.springframework.stereotype.Component

/**
 * Provides string pointers for different Swagger specification objects.
 * Each pointer can be resolved to a specific place in api specification.
 * E.g. getForPathKey("/products") -> "/paths/~1products"
 */
@Component
class SpecificationPointerProvider {

    fun getForPathKey(pathKey: String) = "/paths/" + escaped(pathKey)

    private fun escaped(path: String) =
            path.replace("~", "~0").replace("/", "~1")

}
