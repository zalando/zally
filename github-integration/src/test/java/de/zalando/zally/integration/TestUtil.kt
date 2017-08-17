package de.zalando.zally.integration

import org.apache.commons.io.IOUtils

fun String.loadResource(): String {
    return IOUtils.toString(ClassLoader.getSystemResourceAsStream(this))
}