package de.zalando.zally.util

import javatools.parsers.PlingStemmer

object WordUtil {
    private val PLURAL_WHITELIST = setOf("vat", "apis")

    fun isPlural(word: String): Boolean = PLURAL_WHITELIST.contains(word) || PlingStemmer.isPlural(word)
}
