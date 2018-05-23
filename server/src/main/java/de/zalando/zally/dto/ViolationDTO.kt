package de.zalando.zally.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import de.zalando.zally.rule.api.Severity
import java.util.Arrays.asList

data class ViolationDTO(
    var title: String? = null,
    var description: String? = null,
    var violationType: Severity? = null,
    var ruleLink: String? = null,
    @JsonInclude(Include.NON_NULL) @Deprecated("Use `pointer` instead.") var paths: List<String>? = null,
    @JsonInclude(Include.NON_NULL) var pointer: String? = null
) {
    constructor(
        title: String?,
        description: String?,
        violationType: Severity?,
        ruleLink: String?,
        vararg paths: String
    ) : this(title, description, violationType, ruleLink, asList(*paths))

    @Deprecated("Use `pointer` instead.")
    constructor(
        title: String?,
        description: String?,
        violationType: Severity?,
        ruleLink: String?,
        paths: List<String>?
    ) : this(
        title,
        description,
        violationType,
        ruleLink,
        if (paths?.size == 1) null else paths,
        if (paths?.size == 1) paths[0] else null
    )
}
