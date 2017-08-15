package de.zalando.zally.github

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiValidationController {

    @ResponseBody
    @PostMapping("/api-validation")
    fun validatePullRequest(@RequestBody payload: String) {

    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ApiValidationController::class.java)
    }

}
