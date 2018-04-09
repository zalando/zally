package de.zalando.zally.util.ast;

final class Marker {
    static final String TYPE_X_ZALLY_IGNORE = "x-zally-ignore";

    final String type;
    final String value;

    Marker(String type, String value) {
        this.type = type;
        this.value = value;
    }
}
