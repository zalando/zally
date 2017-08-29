package de.zalando.zally.integration.mock

import net.jadler.mocking.Mocker
import net.jadler.stubbing.Stubber
import net.jadler.stubbing.server.StubHttpServerManager
import org.junit.rules.ExternalResource

class JadlerRule<T>(val mock: T, private val extraSetup: (mock: T) -> Unit = {}) : ExternalResource()
        where T : Mocker,
                T : Stubber,
                T : StubHttpServerManager {

    override fun before() {
        mock.start()
        extraSetup(mock)
    }

    override fun after() {
        mock.close()
    }
}
