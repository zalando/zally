package de.zalando.zally.integration.github

import org.springframework.security.crypto.codec.Hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SecurityUtil {

    fun sign(secret: String, payload: String): String {
        val keySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA1")

        val mac = Mac.getInstance("HmacSHA1")
        mac.init(keySpec)
        val result = mac.doFinal(payload.toByteArray())

        return "sha1=${String(Hex.encode(result))}"
    }
}
