package software.jevera.gchatbot.tasks

import software.jevera.gchatbot.OpenAiClient
import software.jevera.gchatbot.util.ContextExtractor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AskChatGptTask(
    private val openAiClient: OpenAiClient,
    private val contextExtractor: ContextExtractor
) : TaskHandler {
    private val logger = LoggerFactory.getLogger(AskChatGptTask::class.java)
    override val key: String = "askChatGPT"

    override fun run(context: ContextSnapshot): TaskResult {
        val formattedContext = contextExtractor.buildContextString(context)
        val prompt = buildString {
            appendLine("You answer the user's question using the provided Google Chat context.")
            appendLine("Context:")
            append(formattedContext)
        }
        val response = openAiClient.summarize(prompt)
        val answer = openAiClient.firstText(response) ?: run {
            logger.warn("OpenAI returned no answer; falling back to placeholder")
            "I could not generate an answer right now."
        }
        return TaskResult(text = answer)
    }
}
