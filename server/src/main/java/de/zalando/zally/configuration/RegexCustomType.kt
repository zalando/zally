package de.zalando.zally.configuration

import com.typesafe.config.Config
import io.github.config4k.ClassContainer
import io.github.config4k.CustomType
import io.github.config4k.toConfig

// TODO Remove once Config4K supports Regex natively: https://github.com/config4k/config4k/pull/63
object RegexCustomType : CustomType {
    override fun testParse(clazz: ClassContainer): Boolean = clazz.mapperClass == Regex::class

    override fun testToConfig(obj: Any): Boolean = Regex::class.isInstance(obj)

    override fun parse(clazz: ClassContainer, config: Config, name: String): Any? = config.getString(name).toRegex()

    override fun toConfig(obj: Any, name: String): Config = (obj as Regex).pattern.toConfig(name)
}