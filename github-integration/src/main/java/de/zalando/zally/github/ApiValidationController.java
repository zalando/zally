package de.zalando.zally.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiValidationController {

    private static final Logger LOG = LoggerFactory.getLogger(ApiValidationController.class);

    @ResponseBody
    @PostMapping("/api-validation")
    public void validatePullRequest(@RequestBody String payload) {

    }

}
