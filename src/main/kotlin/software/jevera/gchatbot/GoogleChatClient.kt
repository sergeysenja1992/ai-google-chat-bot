package software.jevera.gchatbot

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Component
class GoogleChatClient(
    @Value("\${google.chat.bot-token:}") private val botToken: String,
    @Value("\${google.chat.base-url:https://chat.googleapis.com/v1}") private val baseUrl: String
) {
    private val logger = LoggerFactory.getLogger(GoogleChatClient::class.java)
    private val restTemplate = RestTemplate()

    fun fetchThreadMessages(spaceName: String, threadName: String?, limit: Int): List<ChatMessage> {
        if (botToken.isBlank() || threadName.isNullOrBlank()) {
            return emptyList()
        }
        val endpoint = "$baseUrl/$spaceName/threads/$threadName/messages?filter=creator.type%3D%22HUMAN%22&pageSize=$limit"
        return requestMessages(endpoint)
    }

    fun fetchRecentSpaceMessages(spaceName: String, limit: Int): List<ChatMessage> {
        if (botToken.isBlank()) {
            return emptyList()
        }
        val endpoint = "$baseUrl/$spaceName/messages?filter=creator.type%3D%22HUMAN%22&pageSize=$limit"
        return requestMessages(endpoint)
    }

    private fun requestMessages(url: String): List<ChatMessage> {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        if (botToken.isNotBlank()) {
            headers.setBearerAuth(botToken)
        }
        val entity = HttpEntity<Void>(headers)
        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                ChatMessagesResponse::class.java
            )
            response.body?.messages.orEmpty()
        } catch (ex: RestClientException) {
            logger.error("Failed to fetch messages from {}: {}", url, ex.message)
            emptyList()
        }
    }
}

data class ChatMessagesResponse(
    val messages: List<ChatMessage>? = emptyList()
)
