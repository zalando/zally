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
@SpringBootTest(properties = ["zally.remoteUrls.configFile=nonExistingFile.conf"])
@ActiveProfiles("test")
@EnableConfigurationProperties
class InvalidRemoteUrlsConfigurationTest {

    @Autowired
    private lateinit var remoteUrls: RemoteUrlsConfiguration.RemoteUrls

    /**
     * Remote Urls Configuration is an optional feature and thus Zally should survive a wrongly or not configured filepath.
     */
    @Test
    fun `should survive non existing RemoteUrls configuration file as this is an optional feature`() {
        Assert.assertNotNull(
            "Expecting Remote Urls not null",
            remoteUrls.remoteUrls
        )
        Assert.assertTrue(
            "Expecting Remote Urls to be an empty list.",
            remoteUrls.remoteUrls.isNullOrEmpty()
        )
    }
}
