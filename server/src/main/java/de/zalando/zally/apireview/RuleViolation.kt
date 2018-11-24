package de.zalando.zally.apireview

import com.fasterxml.jackson.annotation.JsonIgnore
import de.zalando.zally.rule.Result
import de.zalando.zally.rule.api.Severity
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class RuleViolation : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    var id: Long? = null

    @JsonIgnore
    @ManyToOne(optional = false)
    var apiReview: ApiReview? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var ruleTitle: String? = null

    @Column(nullable = false)
    var ruleUrl: String? = null

    @Column(nullable = false)
    var description: String? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: Severity? = null

    @Column(nullable = false)
    var locationPointer: String? = null

    @Column(nullable = false)
    var locationLineStart: Int? = null

    @Column(nullable = false)
    var locationLineEnd: Int? = null

    @Deprecated("")
    @Column(nullable = false)
    @get:Deprecated("")
    @set:Deprecated("")
    var occurrence: Int = 0

    /**
     * for Hibernate
     */
    protected constructor() : super()

    constructor(apiReview: ApiReview, result: Result) {
        this.apiReview = apiReview
        this.name = "${result.rule.title} (${result.rule.id})"
        this.ruleTitle = result.rule.title
        this.ruleUrl = result.ruleSet.url(result.rule).toString()
        this.description = result.description
        this.type = result.violationType
        this.locationPointer = result.pointer.toString()
        this.locationLineStart = result.lines?.start
        this.locationLineEnd = result.lines?.endInclusive
        this.occurrence = 1
    }
}
