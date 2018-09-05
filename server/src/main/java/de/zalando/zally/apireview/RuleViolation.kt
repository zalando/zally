package de.zalando.zally.apireview

import com.fasterxml.jackson.annotation.JsonIgnore
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
    @Enumerated(EnumType.STRING)
    var type: Severity? = null

    @Deprecated("")
    @Column(nullable = false)
    @get:Deprecated("")
    @set:Deprecated("")
    var occurrence: Int = 0

    /**
     * for Hibernate
     */
    protected constructor() : super()

    constructor(apiReview: ApiReview, name: String, type: Severity, occurrence: Int) {
        this.apiReview = apiReview
        this.name = name
        this.type = type
        this.occurrence = occurrence
    }
}
