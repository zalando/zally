package de.zalando.zally.apireview

import de.zalando.zally.dto.ApiDefinitionRequest
import de.zalando.zally.rule.Result
import de.zalando.zally.rule.api.Severity
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.Parameter
import org.hibernate.annotations.Type
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Objects
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class ApiReview : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var name: String? = null

    var apiId: String? = null

    @Column(nullable = false)
    var jsonPayload: String? = null

    var apiDefinition: String? = null

    @Column(nullable = false, name = "successfulProcessed")
    var isSuccessfulProcessed: Boolean = false

    @Column(nullable = false)
    var day: LocalDate? = null

    var userAgent: String? = null

    @Column(nullable = false)
    @Type(
        type = "org.jadira.usertype.dateandtime.threeten.PersistentOffsetDateTime",
        parameters = [Parameter(name = "javaZone", value = "UTC")]
    )
    var created: OffsetDateTime? = null

    var numberOfEndpoints: Int = 0
    var mustViolations: Int = 0
    var shouldViolations: Int = 0
    var mayViolations: Int = 0
    var hintViolations: Int = 0

    @OneToMany(mappedBy = "apiReview", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    var ruleViolations: List<RuleViolation>? = null

    /**
     * for Hibernate
     */
    protected constructor() {}

    constructor(
        request: ApiDefinitionRequest,
        userAgent: String = "",
        apiDefinition: String,
        violations: List<Result> = emptyList()
    ) {
        this.jsonPayload = request.toString()
        this.apiDefinition = apiDefinition
        this.isSuccessfulProcessed = StringUtils.isNotBlank(apiDefinition)
        this.created = Instant.now().atOffset(ZoneOffset.UTC)
        this.day = created!!.toLocalDate()
        this.userAgent = userAgent

        this.name = OpenApiHelper.extractApiName(apiDefinition)
        this.apiId = OpenApiHelper.extractApiId(apiDefinition)
        this.ruleViolations = violations
            .map { result ->
                RuleViolation(this, result)
            }

        this.numberOfEndpoints = EndpointCounter.count(apiDefinition)
        this.mustViolations = ruleViolations!!.stream().filter { r -> r.type === Severity.MUST }.count().toInt()
        this.shouldViolations = ruleViolations!!.stream().filter { r -> r.type === Severity.SHOULD }.count().toInt()
        this.mayViolations = ruleViolations!!.stream().filter { r -> r.type === Severity.MAY }.count().toInt()
        this.hintViolations = ruleViolations!!.stream().filter { r -> r.type === Severity.HINT }.count().toInt()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as ApiReview
        return (id == that.id &&
            name == that.name &&
            jsonPayload == that.jsonPayload &&
            apiDefinition == that.apiDefinition &&
            isSuccessfulProcessed == that.isSuccessfulProcessed &&
            day == that.day &&
            created == that.created &&
            numberOfEndpoints == that.numberOfEndpoints &&
            mustViolations == that.mustViolations &&
            shouldViolations == that.shouldViolations &&
            mayViolations == that.mayViolations &&
            hintViolations == that.hintViolations &&
            ruleViolations == that.ruleViolations)
    }

    override fun hashCode(): Int = Objects.hash(
        id, name, jsonPayload, apiDefinition, isSuccessfulProcessed,
        day, created, numberOfEndpoints, mustViolations, shouldViolations, mayViolations,
        hintViolations, ruleViolations
    )
}
