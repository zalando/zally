package de.zalando.zally.apireview;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ApiViolationsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void violationsResponseReferencesFullGuidelinesUrl() throws Exception {
        mvc.perform(
                post("/api-violations")
                        .contentType("application/json")
                        .content("{\"api_definition_raw\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("https://zalando.github.io/restful-api-guidelines")));
    }
}
