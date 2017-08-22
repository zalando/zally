package de.zalando.zally.integration.reports

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.github.PullRequestEvent
import de.zalando.zally.integration.validation.ValidationRepository
import de.zalando.zally.integration.zally.ApiDefinitionResponse
import org.springframework.stereotype.Service

@Service
class ReportService(private val validationRepository: ValidationRepository,
                    private val jsonObjectMapper: ObjectMapper) {

    fun getReport(id: Long): Report {
        val validation = validationRepository.getOne(id)
        val pullRequest = jsonObjectMapper.readValue(validation.pullRequestInfo, PullRequestEvent::class.java)
        val apiDefinitionResponse = jsonObjectMapper.readValue(validation.violations, ApiDefinitionResponse::class.java)
        return Report(validation.id, pullRequest, validation.apiDefinition, apiDefinitionResponse)
    }

}

data class Report(val id: Long?,
                  val pullRequestEvent: PullRequestEvent?,
                  val apiDefinition: String?,
                  val apiDefinitionResponse: ApiDefinitionResponse?) {

}