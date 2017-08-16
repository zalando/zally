package de.zalando.zally.github

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.github.util.SecurityUtil
import org.kohsuke.github.GHEventPayload
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.StringReader
import java.security.MessageDigest

@Service
class GithubService(private val gitHub: GitHub,
                    @Value("\${github.secret}") private val secret: String,
                    @Qualifier("yamlObjectMapper") private val yamlObjectMapper: ObjectMapper) {

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

        return PullRequest(yamlObjectMapper, pullRequestPayload.repository, commitHash)
    }

    fun validatePayload(payload: String, signature: String) {
        val expectedSignature = SecurityUtil.sign(secret, payload)
        if (!MessageDigest.isEqual(signature.toByteArray(), expectedSignature.toByteArray())) {
            throw SecurityException("Signature mismatch")
        }
    }
}
