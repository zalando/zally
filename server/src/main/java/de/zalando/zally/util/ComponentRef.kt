package de.zalando.zally.util

/**
 * Component ref
 */
data class ComponentRef(val type: String, val name: String) {

    companion object {

        fun parse(ref: String): ComponentRef? = try {
            val (_, _, componentType, componentName) = ref.split("/")
            ComponentRef(componentType, componentName)
        } catch (e: Exception) {
            null
        }
    }
}