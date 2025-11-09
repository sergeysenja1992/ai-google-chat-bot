package software.jevera.gchatbot

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class SignatureVerifier(
    @Value("\${google.chat.verification:}") private val verificationToken: String
) {
    private val logger = LoggerFactory.getLogger(SignatureVerifier::class.java)

    fun verify(signatureHeader: String?, body: String) {
        if (verificationToken.isBlank()) {
            logger.warn("google.chat.verification is not configured; skipping signature verification")
            return
        }
        if (signatureHeader.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-Goog-Chat-Signature header")
        }
        val provided = signatureHeader.substringAfter("=")
        val providedBytes = runCatching { Base64.getDecoder().decode(provided) }
            .getOrElse { throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid signature header format") }
        val expected = hmac(body)
        val match = java.security.MessageDigest.isEqual(providedBytes, expected)
        if (!match) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Signature verification failed")
        }
    }

    private fun hmac(body: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(verificationToken.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(key)
        return mac.doFinal(body.toByteArray(Charsets.UTF_8))
    }
}
