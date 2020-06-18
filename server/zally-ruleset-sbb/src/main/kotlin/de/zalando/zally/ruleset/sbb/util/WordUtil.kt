package de.zalando.zally.ruleset.zalando.util

import javatools.parsers.PlingStemmer

internal object WordUtil {
    private val PLURAL_WHITELIST = setOf("vat", "apis")

    fun isPlural(word: String): Boolean = PLURAL_WHITELIST.contains(word) || PlingStemmer.isPlural(word)
}
