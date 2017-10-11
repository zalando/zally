package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionRequest;
import de.zalando.zally.dto.ApiDefinitionWrapper;
import de.zalando.zally.exception.MissingApiDefinitionException;
import de.zalando.zally.exception.UnaccessibleResourceUrlException;
import de.zalando.zally.util.ApiDefinitionDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class ApiDefinitionReader {

    private final Logger log = LoggerFactory.getLogger(ApiDefinitionReader.class);

    // some internal systems add these characters at the end of some urls, don't know why
    private static final String SPECIAL_CHARACTERS_SUFFIX = "%3D%3D";

    private final RestTemplate client;

    @Autowired
    public ApiDefinitionReader(RestTemplate client) {
        this.client = client;
    }

    public ApiDefinitionWrapper read(ApiDefinitionRequest request) throws MissingApiDefinitionException, UnaccessibleResourceUrlException {
        if (request.getApiDefinition() != null) {
            return request.getApiDefinition();
        } else if (request.getApiDefinitionUrl() != null) {
            final String content = readFromUrl(request.getApiDefinitionUrl());
            try {
                return ApiDefinitionDeserializer.getApiDefinitionWrapper(content);
            }catch(IOException ex){
                log.warn("Unable to parse specification: " + request.getApiDefinitionUrl());
                return new ApiDefinitionWrapper(content);
            }
        } else {
            throw new MissingApiDefinitionException();
        }
    }

    private String readFromUrl(String url) throws UnaccessibleResourceUrlException {
        try {
            return client.getForEntity(removeSpecialCharactersSuffix(url), String.class).getBody();
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
