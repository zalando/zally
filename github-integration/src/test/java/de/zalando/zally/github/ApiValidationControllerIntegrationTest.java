package de.zalando.zally.github;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@ActiveProfiles("test")
public class ApiValidationControllerIntegrationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Test
    public void shouldStartPullRequestValidation() throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity("/api-validation", "{}", String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

}