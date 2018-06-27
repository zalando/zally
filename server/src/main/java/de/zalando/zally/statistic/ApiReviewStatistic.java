package de.zalando.zally.statistic;

import de.zalando.zally.apireview.ApiReview;
import de.zalando.zally.apireview.RuleViolation;

import java.util.LinkedList;
import java.util.List;

public class ApiReviewStatistic {

    private String api;
    private String apiId;
    private boolean successful;
    private int numberOfEndpoints;
    private String userAgent;
    private List<RuleViolation> violations;

    ApiReviewStatistic() {
    }

    ApiReviewStatistic(ApiReview apiReview) {
        api = apiReview.getName();
        apiId = apiReview.getApiId();
        successful = apiReview.isSuccessfulProcessed();
        numberOfEndpoints = apiReview.getNumberOfEndpoints();
        userAgent = apiReview.getUserAgent();
        violations = new LinkedList<>(apiReview.getRuleViolations());
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public int getNumberOfEndpoints() {
        return numberOfEndpoints;
    }

    public void setNumberOfEndpoints(int numberOfEndpoints) {
        this.numberOfEndpoints = numberOfEndpoints;
    }

    public List<RuleViolation> getViolations() {
        return violations;
    }

    public void setViolations(List<RuleViolation> violations) {
        this.violations = violations;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getApiId() {
        return apiId;
    }
}
