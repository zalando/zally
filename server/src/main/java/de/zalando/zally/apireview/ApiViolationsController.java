package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionRequest;
import de.zalando.zally.dto.ApiDefinitionResponse;
import de.zalando.zally.dto.ViolationDTO;
import de.zalando.zally.dto.ViolationsCounter;
import de.zalando.zally.exception.MissingApiDefinitionException;
import de.zalando.zally.exception.UnaccessibleResourceUrlException;
import de.zalando.zally.rule.ApiValidator;
import de.zalando.zally.rule.Result;
import de.zalando.zally.rule.RulesPolicy;
import de.zalando.zally.rule.api.Severity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@CrossOrigin
@RestController
public class ApiViolationsController {

    private final ApiValidator rulesValidator;
    private final ApiDefinitionReader apiDefinitionReader;
    private final ApiReviewRepository apiReviewRepository;
    private final ServerMessageService serverMessageService;
    private final RulesPolicy configPolicy;

    @Autowired
    public ApiViolationsController(ApiValidator rulesValidator,
                                   ApiDefinitionReader apiDefinitionReader,
                                   ApiReviewRepository apiReviewRepository,
                                   ServerMessageService serverMessageService,
                                   RulesPolicy configPolicy) {

        this.rulesValidator = rulesValidator;
        this.apiDefinitionReader = apiDefinitionReader;
        this.apiReviewRepository = apiReviewRepository;
        this.serverMessageService = serverMessageService;
        this.configPolicy = configPolicy;
    }

    @ResponseBody
    @PostMapping("/api-violations")
    public ApiDefinitionResponse validate(@RequestBody ApiDefinitionRequest request,
                                          @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        String apiDefinition = retrieveApiDefinition(request);

        RulesPolicy requestPolicy = retrieveRulesPolicy(request);

        List<Result> violations = rulesValidator.validate(apiDefinition, requestPolicy);
        apiReviewRepository.save(new ApiReview(request, userAgent, apiDefinition, violations));

        return buildApiDefinitionResponse(violations, userAgent);
    }

    private RulesPolicy retrieveRulesPolicy(ApiDefinitionRequest request) {
        final List<String> requestRules = request.getIgnoreRules();
        if (requestRules == null) {
            return configPolicy;
        } else {
            return configPolicy.withMoreIgnores(requestRules);
        }
    }

    private String retrieveApiDefinition(ApiDefinitionRequest request) {
        try {
            return apiDefinitionReader.read(request);
        } catch (MissingApiDefinitionException | UnaccessibleResourceUrlException e) {
            apiReviewRepository.save(new ApiReview(request));
            throw e;
        }
    }

    private ApiDefinitionResponse buildApiDefinitionResponse(List<Result> violations, String userAgent) {
        ApiDefinitionResponse response = new ApiDefinitionResponse();
        response.setMessage(serverMessageService.serverMessage(userAgent));
        response.setViolations(violations.stream().map(this::toDto).collect(toList()));
        response.setViolationsCount(buildViolationsCount(violations));
        return response;
    }

    private ViolationDTO toDto(Result violation) {
        return new ViolationDTO(
            violation.getRule().title(),
            violation.getDescription(),
            violation.getViolationType(),
            violation.getRuleSet().url(violation.getRule()).toString(),
            violation.getPaths()
        );
    }

    private Map<String, Integer> buildViolationsCount(List<Result> violations) {
        ViolationsCounter counter = new ViolationsCounter(violations);
        return Arrays.stream(Severity.values()).collect(toMap(
            violationType -> violationType.toString().toLowerCase(),
            counter::getCounter
        ));
    }
}
