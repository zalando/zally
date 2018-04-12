package de.zalando.zally.apireview;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.zalando.zally.rule.api.Severity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class RuleViolation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    private ApiReview apiReview;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Severity type;

    @Deprecated
    @Column(nullable = false)
    private int occurrence;

    /**
     * for Hibernate
     */
    protected RuleViolation() {
        super();
    }

    public RuleViolation(ApiReview apiReview, String name, Severity type, @Deprecated int occurrence) {
        this.apiReview = apiReview;
        this.name = name;
        this.type = type;
        this.occurrence = occurrence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApiReview getApiReview() {
        return apiReview;
    }

    public void setApiReview(ApiReview apiReview) {
        this.apiReview = apiReview;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Severity getType() {
        return type;
    }

    public void setType(Severity type) {
        this.type = type;
    }

    @Deprecated
    public int getOccurrence() {
        return occurrence;
    }

    @Deprecated
    public void setOccurrence(int occurrence) {
        this.occurrence = occurrence;
    }
}
