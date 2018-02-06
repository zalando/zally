package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionRequest;
import de.zalando.zally.exception.MissingApiDefinitionException;
import de.zalando.zally.exception.UnaccessibleResourceUrlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.parseMediaTypes;

@Component
public class ApiDefinitionReader {

    // some internal systems add these characters at the end of some urls, don't know why
    private static final String SPECIAL_CHARACTERS_SUFFIX = "%3D%3D";

    // a whitelist of mime-types to accept when expecting JSON or YAML
    private static final List<MediaType> MEDIA_TYPE_WHITELIST = parseMediaTypes(asList(
        "application/yaml",
        "application/x-yaml",
        "application/vnd.yaml",
        "text/yaml",
        "text/x-yaml",
        "text/vnd.yaml",

        "application/json",
        "application/javascript",
        "text/javascript",
        "text/x-javascript",
        "text/x-json",

        "text/plain"
    ));

    private final RestTemplate client;

    @Autowired
    public ApiDefinitionReader(RestTemplate client) {
        this.client = client;
    }

    public String read(ApiDefinitionRequest request) throws MissingApiDefinitionException, UnaccessibleResourceUrlException {
        if (request.getApiDefinition() != null) {
            return request.getApiDefinition();
        } else if (request.getApiDefinitionUrl() != null) {
            return readFromUrl(request.getApiDefinitionUrl());
        } else {
            throw new MissingApiDefinitionException();
        }
    }

    private String readFromUrl(String url) throws UnaccessibleResourceUrlException {
        try {
            final ResponseEntity<String> response = client.getForEntity(removeSpecialCharactersSuffix(url), String.class);

            final HttpStatus status = response.getStatusCode();
            if (!status.is2xxSuccessful()) {
                throw new UnaccessibleResourceUrlException("Unexpected response " + status.value() + " " + status.getReasonPhrase(), BAD_REQUEST);
            }

            final MediaType contentType = response.getHeaders().getContentType();
            if (MEDIA_TYPE_WHITELIST.stream().noneMatch(contentType::isCompatibleWith)) {
                throw new UnaccessibleResourceUrlException("Unexpected content type in response: " + contentType, BAD_REQUEST);
            }

            return response.getBody();
        } catch (HttpClientErrorException exception) {
            throw new UnaccessibleResourceUrlException(exception.getMessage(), exception.getStatusCode());
        } catch (ResourceAccessException exception) {
            throw new UnaccessibleResourceUrlException("Unknown host: " + exception.getCause().getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    private String removeSpecialCharactersSuffix(String url) {
        return url.endsWith(SPECIAL_CHARACTERS_SUFFIX)
            ? url.substring(0, url.length() - SPECIAL_CHARACTERS_SUFFIX.length())
            : url;
    }
}
