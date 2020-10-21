package org.zalando.zally.configuration

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties
class RemoteUrlsConfigurationTest {

    @Autowired
    private lateinit var remoteUrls: RemoteUrlsConfiguration.RemoteUrls

    @Test
    fun `should read RemoteUrls configuration with OAuth context`() {
        val remoteUrlConfig = remoteUrls.remoteUrls
            .stream()
            .filter { it.host == "some-secured-host-x" }
            .findFirst()
        Assert.assertTrue(
            "Expecting some-secured-host-x as configured remote Url.",
            remoteUrlConfig.isPresent
        )
        Assert.assertEquals(
            "Expecting host-x-client-id as OAuth ClientId for some-secured-host-x in remote Url configuration",
            "host-x-client-id",
            remoteUrlConfig.get().oauth2.clientId
        )
    }
}
