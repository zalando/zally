package org.zalando.zally.apireview

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ServerMessageService(
    @Value("#{'\${zally.cli.deprecatedCliAgents}'.split(',')}") private val deprecatedCliAgents: List<String>,
    @param:Value("\${zally.cli.releasesPage:}") private val releasesPage: String
) {
    private val logger = LoggerFactory.getLogger(ServerMessageService::class.java)

    fun serverMessage(userAgent: String?): String =
        if (userAgent.isNullOrEmpty() || userAgent !in deprecatedCliAgents) {
            ""
        } else {
            logger.info("received request from user-agent {}", userAgent)
            "Please update your CLI: $releasesPage"
        }
}
