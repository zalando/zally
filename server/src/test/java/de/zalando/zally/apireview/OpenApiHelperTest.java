package de.zalando.zally.apireview;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class OpenApiHelperTest {

    @Test
    public void shouldParseApiTitle() {
        String title = OpenApiHelper.extractApiName(
                "openapi: 3.0.1\n" +
                "info:\n" +
                "  title: Awesome API");

        assertThat(title).isEqualTo("Awesome API");
    }

    @Test
    public void shouldReturnNullIfTitleIsNotSet() {
        String title = OpenApiHelper.extractApiName("");

        assertThat(title).isNull();
    }

    @Test
    public void shouldParseApiId() {
        String apiId = OpenApiHelper.extractApiId("\n" +
                "openapi: 3.0.1\n" +
                "info:\n" +
                "  x-api-id: 48aa0090-25ef-11e8-b467-0ed5f89f718b");

        assertThat(apiId).isEqualTo("48aa0090-25ef-11e8-b467-0ed5f89f718b");
    }

    @Test
    public void shouldReturnNullIfApiIdIsNotSet() {
        String apiId = OpenApiHelper.extractApiId("");

        assertThat(apiId).isNull();
    }
}
