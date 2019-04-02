package de.zalando.zally.integration.github

import org.kohsuke.github.GHCommitPointer
import org.kohsuke.github.GHEventPayload
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHUser
import java.net.URL

/**
 * Immutable class that represents important pull request event information
 */
data class PullRequestEvent(
    val pullRequest: PullRequestInfo,
    val repository: RepositoryInfo,
    val sender: UserInfo
) {

    companion object {
        operator fun invoke(event: GHEventPayload.PullRequest): PullRequestEvent {
            return PullRequestEvent(
                    PullRequestInfo(event.pullRequest),
                    RepositoryInfo(event.pullRequest.repository),
                    UserInfo(event.sender))
        }
    }
}

data class PullRequestInfo(
    val url: URL,
    val htmlUrl: URL,
    val title: String,
    val head: CommitPointer
) {

    companion object {
        operator fun invoke(pr: GHPullRequest): PullRequestInfo =
                PullRequestInfo(pr.url, pr.htmlUrl, pr.title, CommitPointer(pr.head))
    }
}

data class CommitPointer(
    val ref: String,
    val sha: String,
    val user: UserInfo
) {

    companion object {
        operator fun invoke(commit: GHCommitPointer) =
                CommitPointer(commit.ref, commit.sha, UserInfo(commit.user))
    }
}

data class RepositoryInfo(
    val name: String,
    val url: URL,
    val htmlUrl: URL
) {

    companion object {
        operator fun invoke(repo: GHRepository): RepositoryInfo =
                RepositoryInfo(repo.name, repo.url, repo.htmlUrl)
    }
}

data class UserInfo(
    val login: String,
    val url: URL,
    val htmlUrl: URL,
    val avatarUrl: String
) {

    companion object {
        operator fun invoke(user: GHUser): UserInfo =
                UserInfo(user.login, user.url, user.htmlUrl, user.avatarUrl)
    }
}