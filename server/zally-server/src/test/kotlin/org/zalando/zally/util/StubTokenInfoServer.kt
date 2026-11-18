package org.zalando.zally.util

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

/**
 * Minimal OAuth2 tokeninfo endpoint for tests running with the `production` profile.
 *
 * The bearer token value selects the answer:
 * - [UID_TOKEN] carries the `uid` scope, [OTHER_SCOPE_TOKEN] only an unrelated one, [NO_SCOPE_TOKEN] none at all.
 * - [SERVER_ERROR_TOKEN], [NOT_FOUND_TOKEN] and [RATE_LIMITED_TOKEN] make the endpoint itself fail with 500, 404 and 429.
 * - [SLOW_TOKEN] is answered only after [SLOW_RESPONSE_MILLIS], longer than the client's read timeout.
 * - Any other token is rejected with 400, like the real endpoint does.
 *
 * The `uid` field matters: the introspector adds the `uid` scope to every token that carries one, so the tokens
 * meant to lack that scope must not have it.
 */
object StubTokenInfoServer {
    const val UID_TOKEN = "uid-token"
    const val OTHER_SCOPE_TOKEN = "other-scope-token"
    const val NO_SCOPE_TOKEN = "no-scope-token"
    const val SERVER_ERROR_TOKEN = "server-error-token"
    const val NOT_FOUND_TOKEN = "not-found-token"
    const val RATE_LIMITED_TOKEN = "rate-limited-token"
    const val SLOW_TOKEN = "slow-token"

    private const val SLOW_RESPONSE_MILLIS = 7_000L

    private val server: HttpServer =
        HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
            createContext("/tokeninfo") { exchange ->
                val authorization = exchange.requestHeaders.getFirst("Authorization")
                if (authorization == "Bearer $SLOW_TOKEN") Thread.sleep(SLOW_RESPONSE_MILLIS)
                val (status, body) =
                    when (authorization) {
                        "Bearer $UID_TOKEN", "Bearer $SLOW_TOKEN" ->
                            200 to """{"access_token":"user","uid":"user-1","scope":["uid"],"expires_in":3600}"""
                        "Bearer $OTHER_SCOPE_TOKEN" -> 200 to """{"access_token":"user","scope":["other"],"expires_in":3600}"""
                        "Bearer $NO_SCOPE_TOKEN" -> 200 to """{"access_token":"user","expires_in":3600}"""
                        "Bearer $SERVER_ERROR_TOKEN" -> 500 to """{"error":"server_error"}"""
                        "Bearer $NOT_FOUND_TOKEN" -> 404 to """{"error":"not_found"}"""
                        "Bearer $RATE_LIMITED_TOKEN" -> 429 to """{"error":"too_many_requests"}"""
                        else -> 400 to """{"error":"invalid_request"}"""
                    }
                val bytes = body.toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(status, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            // Slow answers must not block the fast ones.
            executor =
                java.util.concurrent.Executors
                    .newCachedThreadPool { r -> Thread(r).apply { isDaemon = true } }
            start()
        }

    val url: String get() = "http://localhost:${server.address.port}/tokeninfo"
}
