package de.zalando.zally.apireview;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zally.Application;
import de.zalando.zally.configuration.WebMvcConfiguration;
import de.zalando.zally.dto.ApiDefinitionResponse;
import de.zalando.zally.util.JadlerUtil;
import de.zalando.zally.util.ResourceUtil;
import net.jadler.stubbing.server.jdk.JdkStubHttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.stream.Collectors;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerUsing;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class, RestApiLineNumberTestConfiguration.class}
)
public class RestApiLineNumberTest extends RestApiBaseTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        initJadlerUsing(new JdkStubHttpServer());
    }

    @After
    public void tearDown() {
        closeJadler();
    }

    @Test
    public void shouldProvideCorrectLineNumbersForInRequestJson() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api-violations")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\n\"api_definition\": " + ResourceUtil.resourceToString("fixtures/line_numbers_test.json") + "\n}");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertThat(response.getStatus()).isEqualTo(200);

        String responseContent = response.getContentAsString();
        final ApiDefinitionResponse apiDefinitionResponse = objectMapper.readValue(responseContent, ApiDefinitionResponse.class);

        checkForAvoidTrailingSlashesInJson(apiDefinitionResponse);
        checkForSchemaInJson(apiDefinitionResponse);
        checkForAvoidLinkHeadersRuleInJson(apiDefinitionResponse);
    }

    @Test
    public void shouldProvideCorrectLineNumbersForInRequestYaml() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        final String requestContent = "api_definition: \n" + ResourceUtil.resourceToString("fixtures/line_numbers_test.yaml");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api-violations")
                .contentType(WebMvcConfiguration.MEDIA_TYPE_APP_XYAML)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestContent);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertThat(response.getStatus()).isEqualTo(200);

        String responseContent = response.getContentAsString();
        final ApiDefinitionResponse apiDefinitionResponse = objectMapper.readValue(responseContent, ApiDefinitionResponse.class);

        checkForAvoidTrailingSlashesInJaml(apiDefinitionResponse);
    }

    @Test
    public void shouldProvideCorrectLineNumbersForJsonByUrl() throws Exception {
        String definitionUrl = JadlerUtil.stubResource("fixtures/line_numbers_test.json");

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api-violations")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\n\"api_definition_url\": \"" + definitionUrl + "\"\n}");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertThat(response.getStatus()).isEqualTo(200);

        String responseContent = response.getContentAsString();
        final ApiDefinitionResponse apiDefinitionResponse = objectMapper.readValue(responseContent, ApiDefinitionResponse.class);

        checkForAvoidTrailingSlashesInJson(apiDefinitionResponse);
    }

    @Test
    public void shouldProvideCorrectLineNumbersForYamlByUrl() throws Exception {
        String definitionUrl = JadlerUtil.stubResource("fixtures/line_numbers_test.yaml");

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api-violations")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\n\"api_definition_url\": \"" + definitionUrl + "\"\n}");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertThat(response.getStatus()).isEqualTo(200);

        String responseContent = response.getContentAsString();
        final ApiDefinitionResponse apiDefinitionResponse = objectMapper.readValue(responseContent, ApiDefinitionResponse.class);

        checkForAvoidTrailingSlashesInJaml(apiDefinitionResponse);
    }

    private void checkForSchemaInJson(final ApiDefinitionResponse apiDefinitionResponse) {
        final List<String> schemaPaths = apiDefinitionResponse.getViolations().stream()
                .filter(v -> v.getTitle().equals("OpenAPI 2.0 schema"))
                .flatMap(v -> v.getPaths().stream())
                .collect(Collectors.toList());

        assertThat(schemaPaths).containsExactly(
                "/definitions/Problem/properties/date/type\t\t[line#: 693]",
                "/securityDefinitions/tinbox\t\t[line#: 22]");
    }

    private void checkForAvoidTrailingSlashesInJaml(final ApiDefinitionResponse apiDefinitionResponse) {
        final List<String> paths = apiDefinitionResponse.getViolations().stream()
                .filter(v -> v.getTitle().equals("Avoid Trailing Slashes"))
                .flatMap(v -> v.getPaths().stream())
                .collect(Collectors.toList());

        assertThat(paths).containsExactly(
                "/products/\t\t[line#: 64]",
                "/products/{product_id}/\t\t[line#: 127]",
                "/request-groups/{request_group_id}/updates/\t\t[line#: 437]");
    }

    private void checkForAvoidTrailingSlashesInJson(final ApiDefinitionResponse apiDefinitionResponse) {
        final List<String> paths = apiDefinitionResponse.getViolations().stream()
                .filter(v -> v.getTitle().equals("Avoid Trailing Slashes"))
                .flatMap(v -> v.getPaths().stream())
                .collect(Collectors.toList());

        assertThat(paths).containsExactly(
                "/products/\t\t[line#: 35]",
                "/products/{product_id}/\t\t[line#: 79]",
                "/request-groups/{request_group_id}/updates/\t\t[line#: 360]");
    }

    private void checkForAvoidLinkHeadersRuleInJson(final ApiDefinitionResponse apiDefinitionResponse) {
        final List<String> paths = apiDefinitionResponse.getViolations().stream()
                .filter(v -> v.getTitle().equals("Avoid Link in Header Rule"))
                .flatMap(v -> v.getPaths().stream())
                .collect(Collectors.toList());

        assertThat(paths).containsExactly(
                "/parameters/Link Link\t\t[line#: 471]",
                "/products/{product_id}//202 Link\t\t[line#: 162]");
    }

}
