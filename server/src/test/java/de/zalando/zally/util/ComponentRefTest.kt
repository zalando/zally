package de.zalando.zally.util

import org.junit.Test

class ComponentRefTest {

    @Test
    fun parseRef() {
        val ref = ComponentRef.parse("#/components/schemas/GeneralError")
        assert(ref?.type == "schemas")
        assert(ref?.name == "GeneralError")
    }

    @Test
    fun parseWrongRef() {
        assert(ComponentRef.parse("some wrong ref") == null)
    }
}