package de.zalando.zally.util.ast;

import java.util.Collection;
import java.util.Collections;

final class Marker {
    static final String TYPE_X_ZALLY_IGNORE = "x-zally-ignore";

    final String type;
    final Collection<String> values;

    Marker(String type, Collection<String> values) {
        this.type = type;
        this.values = Collections.unmodifiableCollection(values);
    }
}
