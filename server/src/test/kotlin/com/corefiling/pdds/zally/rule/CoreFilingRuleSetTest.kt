package com.corefiling.pdds.zally.rule

import com.corefiling.pdds.zally.rule.resources.SlashesAtEnd
import de.zalando.zally.rule.api.Rule
import org.junit.Assert.assertEquals
import org.junit.Test

class CoreFilingRuleSetTest {

    val cut = CoreFilingRuleSet()

    @Test
    fun urlBaseHost() {
        assertEquals("wiki.int.corefiling.com", cut.url.host)
    }

    @Test
    fun urlBasePath() {
        assertEquals("/platform/APIGuidelines", cut.url.path)
    }

    @Test
    fun urlRuleBasePath() {
        val rule = SlashesAtEnd::class.java.getAnnotation(Rule::class.java)

        assertEquals("/platform/APIGuidelines", cut.url(rule).path)
    }

    @Test
    fun urlRuleFragment() {
        val rule = SlashesAtEnd::class.java.getAnnotation(Rule::class.java)

        assertEquals("SlashesAtEnd", cut.url(rule).fragment)
    }
}