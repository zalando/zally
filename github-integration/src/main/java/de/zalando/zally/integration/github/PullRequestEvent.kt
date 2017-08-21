package de.zalando.zally.integration.github

import org.kohsuke.github.GHCommitPointer
import org.kohsuke.github.GHEventPayload
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHUser
import java.net.URL

/**
 * Immutable class that represents important pull request event information
 */
data class PullRequestEvent(val pullRequest: PullRequestInfo,
                            val repository: Repository,
                            val sender: User) {

    companion object {
        operator fun invoke(event: GHEventPayload.PullRequest): PullRequestEvent {
            return PullRequestEvent(
                    PullRequestInfo(event.pullRequest.url, event.pullRequest.title, CommitPointer(event.pullRequest.head)),
                    Repository(event.pullRequest.repository),
                    User(event.sender))
        }
    }

}

data class PullRequestInfo(
        val url: URL,
        val title: String,
        val head: CommitPointer)

data class CommitPointer(
        val ref: String,
        val sha: String,
        val user: User) {

    companion object {
        operator fun invoke(commit: GHCommitPointer) = CommitPointer(commit.ref, commit.sha, User(commit.user))
    }
}

data class Repository(val name: String,
                      val url: URL) {

    companion object {
        operator fun invoke(repo: GHRepository): Repository = Repository(repo.name, repo.url)
    }
}

data class User(val login: String,
                val url: URL,
                val avatarUrl: String) {

    companion object {
        operator fun invoke(user: GHUser): User = User(user.login, user.url, user.avatarUrl)
    }
}