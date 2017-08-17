package de.zalando.zally.integration.github

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.zally.Configuration
import org.apache.commons.io.IOUtils
import org.kohsuke.github.GHCommitState
import org.kohsuke.github.GHPullRequestFileDetail
import org.kohsuke.github.GHRepository
import org.kohsuke.github.PagedIterable
import java.nio.charset.StandardCharsets
import java.util.Optional

class PullRequest(private val yamlMapper: ObjectMapper,
                  private val repository: GHRepository,
                  private val commitHash: String,
                  private val changedFiles: PagedIterable<GHPullRequestFileDetail>) {

    private val ZALLY_CONFIGURATION_PATH = ".zally.yaml"

    fun updateCommitState(state: GHCommitState, url: String, description: String) {
        repository.createCommitStatus(commitHash, state, url, description, "Zally")
    }

    private fun getFileContents(path: String): Optional<String> {
        return Optional.ofNullable(repository.getTreeRecursive(commitHash, 1).getEntry(path))
                .map { IOUtils.toString(it.readAsBlob(), StandardCharsets.UTF_8) }
    }

    fun getConfiguration(): Optional<Configuration> {
        return getFileContents(ZALLY_CONFIGURATION_PATH).map {
            yamlMapper.readValue(it, Configuration::class.java)
        }
    }

    fun getSwaggerFile(): Optional<String> {
        return getConfiguration().flatMap {
            getFileContents(it.swaggerPath)
        }
    }

    fun getRepositoryUrl(): String = repository.url.toString()

    fun getChangedFiles(): Optional<List<String>> {
        return Optional.of(changedFiles.map { t -> t.filename })
    }

    fun isAPIChanged(): Boolean {
        val changedFiles = getChangedFiles().get()
        if (changedFiles.contains(ZALLY_CONFIGURATION_PATH) || changedFiles.contains(getConfiguration().get().swaggerPath)) {
            return true
        }
        return false
    }
}
