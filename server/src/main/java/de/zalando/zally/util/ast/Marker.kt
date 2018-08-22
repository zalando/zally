package de.zalando.zally.util.ast

data class Marker(
    val type: String,
    val values: Collection<String>
) {
    companion object {
        const val TYPE_X_ZALLY_IGNORE = "x-zally-ignore"
    }
}
