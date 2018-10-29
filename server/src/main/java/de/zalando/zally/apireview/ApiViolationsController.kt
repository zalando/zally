package de.zalando.zally.apireview

import de.zalando.zally.dto.ApiDefinitionRequest
import de.zalando.zally.dto.ApiDefinitionResponse
import de.zalando.zally.dto.ViolationDTO
import de.zalando.zally.dto.ViolationsCounter
import de.zalando.zally.exception.InaccessibleResourceUrlException
import de.zalando.zally.exception.MissingApiDefinitionException
import de.zalando.zally.rule.ApiValidator
import de.zalando.zally.rule.Result
import de.zalando.zally.rule.RulesPolicy
import de.zalando.zally.rule.api.Severity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class ApiViolationsController @Autowired
constructor(
    private val rulesValidator: ApiValidator,
    private val apiDefinitionReader: ApiDefinitionReader,
    private val apiReviewRepository: ApiReviewRepository,
    private val serverMessageService: ServerMessageService,
    private val configPolicy: RulesPolicy
) {

    @ResponseBody
    @PostMapping("/api-violations")
    fun validate(
        @RequestBody(required = true) request: ApiDefinitionRequest,
        @RequestHeader(value = "User-Agent", required = false) userAgent: String?
    ): ApiDefinitionResponse {
        val apiDefinition = retrieveApiDefinition(request)

        val requestPolicy = retrieveRulesPolicy(request)

        val violations = rulesValidator.validate(apiDefinition!!, requestPolicy)
        apiReviewRepository.save(ApiReview(request, userAgent.orEmpty(), apiDefinition, violations))

        return buildApiDefinitionResponse(violations, userAgent)
    }

    private fun retrieveRulesPolicy(request: ApiDefinitionRequest): RulesPolicy = request.ignoreRules
        ?.let { configPolicy.withMoreIgnores(request.ignoreRules!!) }
        ?: configPolicy

    private fun retrieveApiDefinition(request: ApiDefinitionRequest): String? = try {
        apiDefinitionReader.read(request)
    } catch (e: MissingApiDefinitionException) {
        apiReviewRepository.save(ApiReview(request, "", ""))
        throw e
    } catch (e: InaccessibleResourceUrlException) {
        apiReviewRepository.save(ApiReview(request, "", ""))
        throw e
    }

    private fun buildApiDefinitionResponse(violations: List<Result>, userAgent: String?): ApiDefinitionResponse {
        val response = ApiDefinitionResponse()
        response.message = serverMessageService.serverMessage(userAgent)
        response.violations = violations.map { this.toDto(it) }
        response.violationsCount = buildViolationsCount(violations)
        return response
    }

    private fun toDto(violation: Result): ViolationDTO = ViolationDTO(
        violation.rule.title,
        violation.description,
        violation.violationType,
        violation.ruleSet.url(violation.rule).toString(),
        violation.paths,
        if (violation.pointer == null) null else violation.pointer.toString()
    )

    private fun buildViolationsCount(violations: List<Result>): Map<String, Int> {
        val counter = ViolationsCounter(violations)
        return Severity.values()
            .map { severity ->
                severity.name.toLowerCase() to counter[severity]
            }
            .toMap()
    }
}
