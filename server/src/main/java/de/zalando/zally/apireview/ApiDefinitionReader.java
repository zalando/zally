package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionRequest;
import de.zalando.zally.exception.MissingApiDefinitionException;
import de.zalando.zally.exception.UnaccessibleResourceUrlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.http.MediaType.parseMediaTypes;

@Component
public class ApiDefinitionReader {

    // some internal systems add these characters at the end of some urls, don't know why
    private static final String SPECIAL_CHARACTERS_SUFFIX = "%3D%3D";

    // a whitelist of mime-types to accept when expecting JSON or YAML
    private static final List<MediaType> MEDIA_TYPE_WHITELIST = parseMediaTypes(asList(
        // standard YAML mime-type plus variants
        "application/yaml",
        "application/x-yaml",
        "application/vnd.yaml",
        "text/yaml",
        "text/x-yaml",
        "text/vnd.yaml",

        // standard JSON mime-type plus variants
        "application/json",
        "application/javascript",
        "text/javascript",
        "text/x-javascript",
        "text/x-json",

        // github.com raw content pages issue text/plain content type for YAML
        "text/plain"
    ));

    private final RestTemplate client;

    @Autowired
    public ApiDefinitionReader(RestTemplate client) {
        this.client = client;
    }

    public String read(ApiDefinitionRequest request) throws MissingApiDefinitionException, UnaccessibleResourceUrlException {
        if (request.getApiDefinitionRaw() != null) {
            return request.getApiDefinitionRaw();
        } else if (request.getApiDefinition() != null) {
            return request.getApiDefinition();
        } else if (request.getApiDefinitionUrl() != null) {
            return readFromUrl(request.getApiDefinitionUrl());
        } else {
            throw new MissingApiDefinitionException();
        }
    }

    private String readFromUrl(String url) throws UnaccessibleResourceUrlException {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setAccept(MEDIA_TYPE_WHITELIST);

            final HttpEntity<String> entity = new HttpEntity<>(null, headers);

            final ResponseEntity<String> response = client.exchange(removeSpecialCharactersSuffix(url), HttpMethod.GET, entity, String.class);

            final MediaType contentType = response.getHeaders().getContentType();
            if (MEDIA_TYPE_WHITELIST.stream().noneMatch(contentType::isCompatibleWith)) {
                throw new UnaccessibleResourceUrlException("Unexpected content type while retrieving api definition url: " + contentType, UNSUPPORTED_MEDIA_TYPE);
            }

            return response.getBody();
        } catch (HttpClientErrorException exception) {
            throw new UnaccessibleResourceUrlException(exception.getMessage() + " while retrieving api definition url", exception.getStatusCode());
        } catch (ResourceAccessException exception) {
            throw new UnaccessibleResourceUrlException("Unknown host while retrieving api definition url: " + exception.getCause().getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    private String removeSpecialCharactersSuffix(String url) {
        return url.endsWith(SPECIAL_CHARACTERS_SUFFIX)
            ? url.substring(0, url.length() - SPECIAL_CHARACTERS_SUFFIX.length())
            : url;
    }
}
