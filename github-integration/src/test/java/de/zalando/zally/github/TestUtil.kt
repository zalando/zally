package de.zalando.zally.github

import org.apache.commons.io.IOUtils

fun String.loadResource(): String {
    return IOUtils.toString(ClassLoader.getSystemResourceAsStream(this))
}