package de.zalando.zally.github

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import org.kohsuke.github.GHCommitState
import org.kohsuke.github.GHRepository

import java.io.IOException
import java.nio.charset.StandardCharsets

class PullRequest
constructor(private val yamlMapper: ObjectMapper, private val repository: GHRepository, private val commitHash: String) {
    val configuration: Configuration

    init {
        this.configuration = fetchConfiguration()
    }

    fun updateCommitState(state: GHCommitState, url: String, description: String, context: String) {
        repository.createCommitStatus(commitHash, state, url, description, context)
    }

    private fun getFileContents(path: String): String {
        return IOUtils.toString(
                repository.getTree(commitHash).getEntry(path).readAsBlob(),
                StandardCharsets.UTF_8
        )
    }

    private fun fetchConfiguration(): Configuration {
        return yamlMapper.readValue(
                getFileContents(ZALLY_CONFIGURATION_PATH),
                Configuration::class.java
        )
    }

    val swaggerFile: String
        get() = getFileContents(configuration.swaggerPath)

    companion object {
        private val ZALLY_CONFIGURATION_PATH = ".zally.yaml"
    }
}
