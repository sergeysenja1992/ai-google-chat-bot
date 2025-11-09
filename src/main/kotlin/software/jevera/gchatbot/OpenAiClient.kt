package software.jevera.gchatbot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import software.jevera.gchatbot.config.ApplicationProperties

@Component
class OpenAiClient(
    private val applicationProperties: ApplicationProperties
) {
    private val logger = LoggerFactory.getLogger(OpenAiClient::class.java)
    private val restTemplate = RestTemplate()
    private val openAi = applicationProperties.openai

    fun chooseTask(prompt: String): RouterResponse? {
        val endpoint = "${openAi.baseUrl}/responses"
        val request = OpenAiRequest(model = openAi.routerModel, input = prompt)
        return execute(endpoint, request)
    }

    fun summarize(prompt: String): RouterResponse? {
        val endpoint = "${openAi.baseUrl}/responses"
        val request = OpenAiRequest(model = openAi.summaryModel, input = prompt)
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
        if (openAi.apiKey.isBlank()) {
            logger.warn("openai.api-key is not set; skipping call to {}", endpoint)
            return null
        }
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(openAi.apiKey)
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
