package de.zalando.zally.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zally.dto.ApiDefinitionWrapper;
import de.zalando.zally.rule.ObjectTreeReader;

import java.io.IOException;

public class ApiDefinitionDeserializer extends JsonDeserializer<ApiDefinitionWrapper> {

    @Override
    public ApiDefinitionWrapper deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        return getApiDefinitionWrapper(jp, null);
    }

    public static ApiDefinitionWrapper getApiDefinitionWrapper(String apiContent) throws IOException {
        final ObjectTreeReader treeReader = new ObjectTreeReader();
        final JsonParser parser = treeReader.getParser(apiContent);
        return getApiDefinitionWrapper(parser, apiContent);
    }

    private static ApiDefinitionWrapper getApiDefinitionWrapper(JsonParser parser, String apiContent) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNodeFactoryLocationListener locationListener = new JsonNodeFactoryLocationListener(parser, apiContent == null);
        mapper.setNodeFactory(locationListener);

        final JsonNode apiDefNode = mapper.readTree(parser);
        final String apiDefinition = apiContent == null ? mapper.writeValueAsString(apiDefNode) : apiContent;

        return new ApiDefinitionWrapper(apiDefinition, locationListener.createLocationResolver(apiDefNode));
    }

}