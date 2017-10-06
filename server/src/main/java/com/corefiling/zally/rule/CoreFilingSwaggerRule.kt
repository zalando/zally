package com.corefiling.zally.rule

import de.zalando.zally.rule.SwaggerRule

abstract class CoreFilingSwaggerRule : SwaggerRule() {
    override val url = page(javaClass) + "#" + code(javaClass)
    override val code = code(javaClass)
    override val guidelinesCode = code(javaClass)
}

fun <T> code(clazz: Class<T>): String {
    return clazz.simpleName
}

fun <T> page(clazz: Class<T>): String {
    val name = lastSubpackage(clazz)
    return name.substring(0, 1).toUpperCase() + name.substring(1)
}

fun <T> lastSubpackage(clazz: Class<T>): String {
    val name = clazz.`package`.name
    return name.substring(name.lastIndexOf('.')+1)
}