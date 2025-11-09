package software.jevera.gchatbot.tasks

import software.jevera.gchatbot.OpenAiClient
import software.jevera.gchatbot.util.ContextExtractor
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TaskSelector(
    private val taskCatalog: TaskCatalog,
    private val openAiClient: OpenAiClient,
    private val contextExtractor: ContextExtractor
) {
    private val logger = LoggerFactory.getLogger(TaskSelector::class.java)
    private val objectMapper = ObjectMapper()

    fun proposeTask(context: ContextSnapshot): ProposedTask {
        val preview = contextExtractor.buildPreview(context)
        val catalogPrompt = taskCatalog.tasks.joinToString(separator = "\n") { "- ${it.key}: ${it.description}" }
        val prompt = buildString {
            appendLine("You are a router. Choose exactly one task from the catalog that best satisfies the user's intent.")
            appendLine("Return JSON with keys taskKey, confidence, rationale.")
            appendLine("TASK_CATALOG:")
            appendLine(catalogPrompt)
            appendLine("CONTEXT:")
            append(preview)
        }
        val response = openAiClient.chooseTask(prompt)
        val text = openAiClient.firstText(response)
        val parsed = runCatching { text?.let { objectMapper.readTree(it) } }.getOrNull()
        val taskKey = parsed?.path("taskKey")?.asText()?.takeIf { it.isNotBlank() }
            ?: defaultTaskKey(context)
        val confidence = parsed?.path("confidence")?.asDouble()?.takeIf { !it.isNaN() } ?: 0.5
        val rationale = parsed?.path("rationale")?.asText()?.takeIf { it.isNotBlank() }
            ?: "Selected by fallback logic."
        return ProposedTask(
            key = taskKey,
            confidence = confidence.coerceIn(0.0, 1.0),
            rationale = rationale,
            contextPreview = preview
        )
    }

    private fun defaultTaskKey(context: ContextSnapshot): String {
        val threadText = context.threadMessages.joinToString("\n") { it.text.orEmpty() }
        return if (threadText.contains("time", ignoreCase = true)) {
            "currentTime"
        } else {
            "askChatGPT"
        }
    }
}
