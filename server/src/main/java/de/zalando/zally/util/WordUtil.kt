package de.zalando.zally.util

import javatools.parsers.PlingStemmer

import java.util.HashSet

import java.util.Arrays.asList

object WordUtil {
    private val PLURAL_WHITELIST = HashSet(asList("vat", "apis"))

    fun isPlural(word: String): Boolean {
        return if (PLURAL_WHITELIST.contains(word)) {
            true
        } else PlingStemmer.isPlural(word)
    }
}
