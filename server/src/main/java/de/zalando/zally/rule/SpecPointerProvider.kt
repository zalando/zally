package de.zalando.zally.rule

import de.zalando.zally.util.getVerb
import io.swagger.models.Operation
import io.swagger.models.Path

/**
 * Provides string pointers for different Swagger specification objects.
 * Each pointer can be resolved to a specific place in api specification.
 * E.g. forPath("/products") -> "/paths/~1products"
 */
object SpecPointerProvider {

    fun forPath(pathKey: String) = "/paths/" + escaped(pathKey)

    fun forOperation(pathKey: String, path: Path, operation: Operation) = forPath(pathKey) + "/" + path.getVerb(operation)

    private fun escaped(path: String) =
            path.replace("~", "~0").replace("/", "~1")

}
