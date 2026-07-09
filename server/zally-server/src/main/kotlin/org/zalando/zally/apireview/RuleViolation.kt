package org.zalando.zally.apireview

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.zalando.zally.core.Result
import org.zalando.zally.rule.api.Severity
import java.io.Serializable

@Entity
class RuleViolation(
    @Suppress("unused")
    @JsonIgnore
    @ManyToOne(optional = false)
    val apiReview: ApiReview,
    result: Result
) : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Long = 0

    @Column(nullable = false)
    val name: String = "${result.title} (${result.id})"

    @Column(nullable = false)
    val ruleTitle: String = result.title

    @Column(nullable = false)
    val ruleUrl: String = result.url.toString()

    @Column(nullable = false)
    val description: String = result.description

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: Severity = result.violationType

    @Column(nullable = false)
    val locationPointer: String = result.pointer.toString()

    @Column(nullable = true)
    val locationLineStart: Int? = result.lines?.start

    @Column(nullable = true)
    val locationLineEnd: Int? = result.lines?.endInclusive

    @Deprecated("")
    @Column(nullable = false)
    @get:Deprecated("")
    val occurrence: Int = 1
}
