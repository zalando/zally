package org.zalando.zally

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.management.ManagementFactory
import javax.management.ObjectName

class ApplicationTest {
    @Test
    fun `main starts the application and it shuts down again`() {
        val admin = ObjectName("org.springframework.boot:type=Admin,name=SpringApplication")
        val server = ManagementFactory.getPlatformMBeanServer()

        main(
            arrayOf(
                "--spring.profiles.active=test,limited-rules",
                "--spring.application.admin.enabled=true",
                "--server.port=0",
                "--management.server.port=0",
                "--spring.main.banner-mode=off",
            ),
        )

        try {
            assertThat(server.isRegistered(admin)).isTrue()
            assertThat(server.getAttribute(admin, "Ready")).isEqualTo(true)
        } finally {
            server.invoke(admin, "shutdown", null, null)
        }
    }
}
