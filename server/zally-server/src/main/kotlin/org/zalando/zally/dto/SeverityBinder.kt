package org.zalando.zally.dto

import org.springframework.util.StringUtils
import org.zalando.zally.rule.api.Severity
import java.beans.PropertyEditorSupport

class SeverityBinder : PropertyEditorSupport() {

    @Throws(IllegalArgumentException::class)
    override fun setAsText(text: String) {
        val value = if (StringUtils.hasText(text)) Severity.valueOf(text.uppercase()) else null
        setValue(value)
    }
}
