package software.jevera.gchatbot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Component
class OpenAiClient(
    @Value("\${openai.api-key:}") private val apiKey: String,
    @Value("\${openai.base-url:https://api.openai.com/v1}") private val baseUrl: String,
    @Value("\${openai.router-model:gpt-4.1-mini}") private val routerModel: String,
    @Value("\${openai.summary-model:gpt-4.1-mini}") private val summaryModel: String
) {
    private val logger = LoggerFactory.getLogger(OpenAiClient::class.java)
    private val restTemplate = RestTemplate()

    fun chooseTask(prompt: String): RouterResponse? {
        val endpoint = "$baseUrl/responses"
        val request = OpenAiRequest(model = routerModel, input = prompt)
        return execute(endpoint, request)
    }

    fun summarize(prompt: String): RouterResponse? {
        val endpoint = "$baseUrl/responses"
        val request = OpenAiRequest(model = summaryModel, input = prompt)
        return execute(endpoint, request)
    }

    fun firstText(response: RouterResponse?): String? {
        if (response == null) return null
        response.output.orEmpty().forEach { message ->
            message.content.orEmpty().firstOrNull { it.type == "output_text" || it.type == "text" }
                ?.text
                ?.let { return it }
        }
        return null
    }

    private fun execute(endpoint: String, body: OpenAiRequest): RouterResponse? {
        if (apiKey.isBlank()) {
            logger.warn("openai.api-key is not set; skipping call to {}", endpoint)
            return null
        }
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(apiKey)
        val requestEntity = HttpEntity(body, headers)
        return try {
            restTemplate.postForObject(endpoint, requestEntity, RouterResponse::class.java)
        } catch (ex: RestClientException) {
            logger.error("Failed to call OpenAI: {}", ex.message)
            null
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAiRequest(
    val model: String,
    val input: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RouterResponse(
    val id: String? = null,
    val output: List<RouterMessage>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RouterMessage(
    val id: String? = null,
    val role: String? = null,
    val content: List<RouterMessageContent>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RouterMessageContent(
    val type: String? = null,
    val text: String? = null,
    @JsonProperty("refusal") val refusal: Any? = null
)
