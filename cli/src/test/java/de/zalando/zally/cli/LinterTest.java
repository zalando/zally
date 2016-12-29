package de.zalando.zally.cli;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class LinterTest {
    @Injectable
    private ZallyApiClient client;

    @Injectable
    private ResultPrinter resultPrinter;

    @Tested
    private Linter linter;

    @Test
    public void returnsTrueWhenNoViolationsAreReturned() throws Exception {
        JsonObject testResult = new JsonObject();
        JsonArray violations = new JsonArray();
        testResult.add("violations", violations);

        new Expectations() {{
            client.validate(anyString); result = testResult;
        }};

        linter = new Linter(client, resultPrinter);
        Boolean result = linter.lint(getJsonReader());

        assertEquals(true, result);

        new Verifications() {{
            List<JsonObject> mustList;
            List<JsonObject> shouldList;
            List<JsonObject> couldList;

            resultPrinter.printViolations(mustList = withCapture(), "must");
            resultPrinter.printViolations(shouldList = withCapture(), "should");
            resultPrinter.printViolations(couldList = withCapture(), "could");
            resultPrinter.printSummary();;

            assertEquals(0, mustList.size());
            assertEquals(0, shouldList.size());
            assertEquals(0, couldList.size());
        }};
    }

    @Test
    public void returnsTrueWhenOnlyShouldAndCouldViolationFound() throws Exception {
        JsonObject testResult = new JsonObject();
        JsonArray violations = new JsonArray();
        violations.add(getViolation("should", "should"));
        violations.add(getViolation("could", "could"));
        testResult.add("violations", violations);

        new Expectations() {{
            client.validate(anyString); result = testResult;
        }};

        linter = new Linter(client, resultPrinter);
        Boolean result = linter.lint(getJsonReader());

        assertEquals(true, result);

        new Verifications() {{
            List<JsonObject> mustList;
            List<JsonObject> shouldList;
            List<JsonObject> couldList;

            resultPrinter.printViolations(mustList = withCapture(), "must");
            resultPrinter.printViolations(shouldList = withCapture(), "should");
            resultPrinter.printViolations(couldList = withCapture(), "could");
            resultPrinter.printSummary();;

            assertEquals(0, mustList.size());
            assertEquals(1, shouldList.size());
            assertEquals(1, couldList.size());

            assertEquals("should", shouldList.get(0).get("title").asString());
            assertEquals("could", couldList.get(0).get("title").asString());
        }};
    }

    @Test
    public void returnsFalseWhenMustViolationsFound() throws Exception {
        JsonObject testResult = new JsonObject();
        JsonArray violations = new JsonArray();
        violations.add(getViolation("must", "must"));
        testResult.add("violations", violations);

        new Expectations() {{
            client.validate(anyString); result = testResult;
        }};

        linter = new Linter(client, resultPrinter);
        Boolean result = linter.lint(getJsonReader());

        assertEquals(false, result);

        new Verifications() {{
            List<JsonObject> mustList;
            List<JsonObject> shouldList;
            List<JsonObject> couldList;

            resultPrinter.printViolations(mustList = withCapture(), "must");
            resultPrinter.printViolations(shouldList = withCapture(), "should");
            resultPrinter.printViolations(couldList = withCapture(), "could");
            resultPrinter.printSummary();;

            assertEquals(1, mustList.size());
            assertEquals(0, shouldList.size());
            assertEquals(0, couldList.size());

            assertEquals("must", mustList.get(0).get("title").asString());
        }};
    }

    private SpecsReader getJsonReader() {
        String fixture = "{\"hello\":\"world\"}";
        InputStream inputStream = new ByteArrayInputStream(fixture.getBytes());
        return new JsonReader(new InputStreamReader(inputStream));
    }

    private JsonObject getViolation(String title, String type) {
        JsonObject violation = new JsonObject();
        violation.add("title", title);
        violation.add("description", "Test Description: " + title);
        violation.add("violation_type", type);
        return violation;
    }
}
