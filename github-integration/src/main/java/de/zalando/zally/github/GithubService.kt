package de.zalando.zally.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import de.zalando.zally.github.util.SecurityUtil
import org.kohsuke.github.GHEventPayload
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.StringReader
import java.security.MessageDigest


@Service
class GithubService
constructor(private val gitHub: GitHub, @Value("\${zally.secret}") private val secret: String) {
    private val yamlMapper = ObjectMapper(YAMLFactory())

    fun parsePayload(payload: String, signature: String): PullRequest {
        validatePayload(payload, signature)

        val pullRequestPayload = gitHub.parseEventPayload(
                StringReader(payload),
                GHEventPayload.PullRequest::class.java
        )

        val commitHash = pullRequestPayload
                .pullRequest
                .head
                .sha

        return PullRequest(yamlMapper, pullRequestPayload.repository, commitHash)
    }

    fun validatePayload(payload: String, signature: String) {
        val expectedSignature = SecurityUtil.sign(secret, payload)
        if (!MessageDigest.isEqual(signature.toByteArray(), expectedSignature.toByteArray())) {
            throw SecurityException("Signature mismatch")
        }
    }
}
