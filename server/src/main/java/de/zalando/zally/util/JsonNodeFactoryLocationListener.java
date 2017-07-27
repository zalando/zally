package de.zalando.zally.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.util.RawValue;
import de.zalando.zally.dto.LocationResolver;
import de.zalando.zally.dto.MapLocationResolver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonNodeFactoryLocationListener extends JsonNodeFactory {

    private static final int lineCorrection = -1;

    private final JsonParser parser;
    // We cannot use Map or Set here since JsonNode has incorrect hashcode() till full tree is constructed
    private final List<NodeLocation> locationList = new ArrayList<>();

    private static class NodeLocation{
        private JsonNode node;
        private int location;

        private NodeLocation(final JsonNode node, final int location) {
            this.node = node;
            this.location = location;
        }
    }

    public JsonNodeFactoryLocationListener(final JsonParser parser) {
        this.parser = parser;
    }

    public LocationResolver createLocationResolver(JsonNode rootNode){
        Map<JsonNode, Integer> locationMap = new HashMap<>(locationList.size());
        for(NodeLocation loc: locationList){
            locationMap.put(loc.node, loc.location);
        }
        return new MapLocationResolver(rootNode, locationMap);
    }

    private <T extends JsonNode> T recordLocation(T node) {
        final int lineNr = parser.getTokenLocation().getLineNr() + lineCorrection;
        locationList.add(new NodeLocation(node, lineNr));
        return node;
    }

    @Override
    public BooleanNode booleanNode(final boolean v) {
        return recordLocation(super.booleanNode(v));
    }

    @Override
    public NullNode nullNode() {
        return recordLocation(super.nullNode());
    }

    @Override
    public NumericNode numberNode(final byte v) {
        return recordLocation(super.numberNode(v));
    }

    @Override
    public ValueNode numberNode(final Byte value) {
        return recordLocation(super.numberNode(value));
    }

    @Override
    public NumericNode numberNode(final short v) {
        return recordLocation(super.numberNode(v));
    }

    @Override
    public ValueNode numberNode(final Short value) {
        return recordLocation(super.numberNode(value));
    }

    @Override
    public NumericNode numberNode(final int v) {
        return recordLocation(super.numberNode(v));
    }

    @Override
    public ValueNode numberNode(final Integer value) {
        return recordLocation(super.numberNode(value));
    }

    @Override
    public NumericNode numberNode(final long v) {
        return recordLocation(super.numberNode(v));
    }

    @Override
    public ValueNode numberNode(final Long value) {
        return recordLocation(super.numberNode(value));
    }

    @Override
    public NumericNode numberNode(final BigInteger v) {
        return recordLocation(super.numberNode(v));
    }

    @Override
    public NumericNode numberNode(final float v) {
        return recordLocation(super.numberNode(v));
    }

    @Override
    public ValueNode numberNode(final Float value) {
        return recordLocation(super.numberNode(value));
    }

    @Override
    public NumericNode numberNode(final double v) {
        return recordLocation(super.numberNode(v));
    }

    @Override
    public ValueNode numberNode(final Double value) {
        return recordLocation(super.numberNode(value));
    }

    @Override
    public NumericNode numberNode(final BigDecimal v) {
        return recordLocation(super.numberNode(v));
    }

    @Override
    public TextNode textNode(final String text) {
        return recordLocation(super.textNode(text));
    }

    @Override
    public BinaryNode binaryNode(final byte[] data) {
        return recordLocation(super.binaryNode(data));
    }

    @Override
    public BinaryNode binaryNode(final byte[] data, final int offset, final int length) {
        return recordLocation(super.binaryNode(data, offset, length));
    }

    @Override
    public ArrayNode arrayNode() {
        return recordLocation(super.arrayNode());
    }

    @Override
    public ArrayNode arrayNode(final int capacity) {
        return recordLocation(super.arrayNode(capacity));
    }

    @Override
    public ObjectNode objectNode() {
        return recordLocation(super.objectNode());
    }

    @Override
    public ValueNode pojoNode(final Object pojo) {
        return recordLocation(super.pojoNode(pojo));
    }

    @Override
    public ValueNode rawValueNode(final RawValue value) {
        return recordLocation(super.rawValueNode(value));
    }
}
