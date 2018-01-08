package de.zalando.zally.dto;

import de.zalando.zally.rule.api.Severity;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ViolationTypeBinderTest {
    @Test(expected = IllegalArgumentException.class)
    public void shouldRaiseIllegalArgumentExceptionWhenTypeIsNotFound() {
        final SeverityBinder typeBinder = new SeverityBinder();
        typeBinder.setAsText("SOLUTION");
    }

    @Test
    public void shouldSetValueWhenTypeIsFound() {
        final SeverityBinder typeBinder = new SeverityBinder();
        final String[] allowedTypes = {"Must", "MUST", "must", "SHOULD", "MAY", "HINT"};

        for (String allowedType : allowedTypes) {
            Severity expectedType = Severity.valueOf(allowedType.toUpperCase());
            typeBinder.setAsText(allowedType);

            assertThat(typeBinder.getValue()).isEqualTo(expectedType);
        }
    }
}
