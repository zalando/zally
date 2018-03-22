package de.zalando.zally;

import java.util.Map;

import io.swagger.models.Info;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;


import de.zalando.zally.rule.model.SwaggerProxy;

public class ProxyTest {

    @Test
    public void invokeAsUsualWorks() {
        Swagger swagger = new Swagger();
        swagger.setInfo(new Info().title("my title"));
        SwaggerProxy proxy = new SwaggerProxy(stringObjectMap -> true);

        Swagger proxied = proxy.build(swagger);
        assertThat(proxied.getInfo().getTitle()).isEqualTo("my title");
    }

    @Test
    public void filterInfoByVendorExtensions() {
        Swagger swagger = new Swagger();
        Info info = new Info().title("my title");
        info.setVendorExtension("x-zally-ignore", "1");
        swagger.setInfo(info);

        SwaggerProxy proxy = new SwaggerProxy(map -> !map.containsKey("x-zally-ignore"));
        Swagger proxied = proxy.build(swagger);

        assertThat(proxied.getInfo()).isNull();
    }

    @Test
    public void pathWithExtensionIgnored() {
        Swagger swagger = TestUtilKt.getFixture("path_with_extension.yaml");
        SwaggerProxy proxy = new SwaggerProxy(map -> !map.containsKey("x-zally-ignore"));
        Swagger proxied = proxy.build(swagger);

        Map<String, Path> paths = proxied.getPaths();
        assertThat(paths.size()).isEqualTo(2);
        assertThat(paths.keySet().contains("/valid-path")).isTrue();
        assertThat(paths.keySet().contains("/another-valid-path")).isTrue();
    }
}
