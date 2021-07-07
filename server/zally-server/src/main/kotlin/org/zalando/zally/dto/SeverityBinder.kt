package org.zalando.zally.dto

import org.zalando.zally.rule.api.Severity
import org.springframework.util.StringUtils

import java.beans.PropertyEditorSupport

class SeverityBinder : PropertyEditorSupport() {

    @Throws(IllegalArgumentException::class)
    override fun setAsText(text: String) {
        val value = if (StringUtils.hasText(text)) Severity.valueOf(text.toUpperCase()) else null
        setValue(value)
    }
}
