package de.zalando.zally.integration.github

import com.fasterxml.jackson.databind.ObjectMapper
import org.kohsuke.github.GHEventPayload
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.StringReader
import java.security.MessageDigest

@Service
class GithubService(
    private val gitHub: GitHub,
    @Value("\${github.secret}") private val secret: String,
    @Qualifier("yamlObjectMapper") private val yamlObjectMapper: ObjectMapper
) {

    fun parsePayload(payload: String, signature: String): PullRequest {
        validatePayload(payload, signature)

        val pullRequestPayload = gitHub.parseEventPayload(
                StringReader(payload),
                GHEventPayload.PullRequest::class.java)

        val pullRequestEvent = PullRequestEvent(pullRequestPayload)

        // Get the list of changed files and set here.
        val changedFiles = pullRequestPayload.pullRequest.listFiles()
        return PullRequest(yamlObjectMapper, pullRequestPayload.repository, pullRequestEvent, changedFiles)
    }

    fun validatePayload(payload: String, signature: String) {
        val expectedSignature = SecurityUtil.sign(secret, payload)
        if (!MessageDigest.isEqual(signature.toByteArray(), expectedSignature.toByteArray())) {
            throw SecurityException("Signature mismatch")
        }
    }
}
