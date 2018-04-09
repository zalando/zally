package de.zalando.zally.util.ast;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * ReverseAst holds meta information for nodes of a Swagger or OpenApi object.
 */
public class ReverseAst {
    /**
     * Creates a new instance of ReverseAstBuilder from a Swagger or OpenApi object.
     *
     * @param root Swagger or OpenApi instance.
     * @return ReverseAstBuilder instance.
     */
    public static ReverseAstBuilder fromObject(Object root) {
        return new ReverseAstBuilder(root);
    }

    private final Map<Object, Meta> map;

    ReverseAst(Map<Object, Meta> map) {
        this.map = map;
    }

    @Nullable
    public String getPointer(Object key) {
        Meta meta = this.map.get(key);
        if (meta != null) {
            return meta.pointer;
        }
        return null;
    }

    public boolean isIgnored(Object key) {
        Meta meta = this.map.get(key);
        return meta != null && meta.marker != null && Marker.TYPE_X_ZALLY_IGNORE.equals(meta.marker.type);
    }

    @Nullable
    public String getIgnoreValue(Object key) {
        Meta meta = this.map.get(key);
        if (meta != null && meta.marker != null && Marker.TYPE_X_ZALLY_IGNORE.equals(meta.marker.type)) {
            return meta.marker.value;
        }
        return null;
    }
}
