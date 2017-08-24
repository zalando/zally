package de.zalando.zally.integration.validation

import de.zalando.zally.integration.config.StringJsonUserType
import org.hibernate.annotations.Parameter
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@TypeDefs(value = TypeDef(name = "StringJsonObject", typeClass = StringJsonUserType::class))
@EntityListeners(value = AuditingEntityListener::class)
@Entity
data class PullRequestValidation(

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "pull_request_validation_id")
        var id: Long? = null,

        @Type(type = "StringJsonObject")
        var pullRequestInfo: String? = null,

        @OneToMany(fetch = FetchType.EAGER, mappedBy = "pullRequestValidation", cascade = arrayOf(CascadeType.ALL),
                orphanRemoval = true)
        var apiValidations: List<ApiValidation> = ArrayList(),

        @CreatedDate
        @Column(nullable = false)
        @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentOffsetDateTime",
                parameters = arrayOf(Parameter(name = "javaZone", value = "UTC")))
        var createdOn: OffsetDateTime? = null

)