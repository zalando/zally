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

// open for mocking 
open class PullRequest(
    private val yamlMapper: ObjectMapper,
    private val repository: GHRepository,
    val eventInfo: PullRequestEvent,
    private val changedFiles: PagedIterable<GHPullRequestFileDetail>
) {

    private val ZALLY_CONFIGURATION_PATH = ".zally.yaml"

    open fun updateCommitState(state: GHCommitState, url: String?, description: String) {
        repository.createCommitStatus(commitHash(), state, url, description, "Zally")
    }

    open fun getConfiguration(): Optional<Configuration> =
            getFileContents(ZALLY_CONFIGURATION_PATH)
            .map { yamlMapper.readValue(it, Configuration::class.java) }

    open fun getApiDefinitions(): Map<String, Optional<String>> {
        val configuration = getConfiguration()

        if (!configuration.isPresent) {
            return emptyMap()
        }

        return getConfiguration().get().apiDefinitions.map {
            it to getFileContents(it)
        }.toMap()
    }

    open fun isAPIChanged(): Boolean {
        val changedFiles = getChangedFiles()
        return changedFiles.contains(ZALLY_CONFIGURATION_PATH) ||
                getConfiguration().get().apiDefinitions.intersect(changedFiles).isNotEmpty()
    }

    private fun getFileContents(path: String): Optional<String> =
            Optional.ofNullable(repository.getTreeRecursive(commitHash(), 1).getEntry(path))
                    .map { IOUtils.toString(it.readAsBlob(), StandardCharsets.UTF_8) }

    private fun getChangedFiles(): List<String> = changedFiles.map { t -> t.filename }

    private fun commitHash(): String = eventInfo.pullRequest.head.sha
}