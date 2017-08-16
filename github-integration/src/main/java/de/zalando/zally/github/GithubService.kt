package de.zalando.zally.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.kohsuke.github.GHEventPayload
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.codec.Hex
import org.springframework.stereotype.Service

import java.io.StringReader
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


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
        val keySpec = SecretKeySpec(secret.toByteArray(),"HmacSHA1")

        val mac = Mac.getInstance("HmacSHA1")
        mac.init(keySpec)
        val result = mac.doFinal(payload.toByteArray())

        if (!MessageDigest.isEqual(signature.toByteArray(), "sha1=${String(Hex.encode(result))}".toByteArray())) {
            throw SecurityException("Signature mismatch")
        }
    }
}
