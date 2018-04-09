package de.zalando.zally.util.ast;

/**
 * Meta information for an object node in a Swagger or OpenApi object.
 */
final class Meta {
    final String pointer;
    final Marker marker;

    Meta(String pointer, Marker marker) {
        this.pointer = pointer;
        this.marker = marker;
    }
}
