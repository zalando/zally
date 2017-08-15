package de.zalando.zally.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.kohsuke.github.GHEventPayload
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.io.IOException
import java.io.StringReader

@Service
class GithubService
constructor(private val gitHub: GitHub) {
    private val yamlMapper = ObjectMapper(YAMLFactory())

    fun parsePayload(payload: String): PullRequest {
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
}
