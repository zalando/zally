package de.zalando.zally.dto;

import de.zalando.zally.rule.api.Severity;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

public class SeverityBinder extends PropertyEditorSupport {

    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        final Severity value = StringUtils.hasText(text) ? Severity.valueOf(text.toUpperCase()) : null;
        setValue(value);
    }
}
