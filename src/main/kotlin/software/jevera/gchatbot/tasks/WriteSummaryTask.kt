package software.jevera.gchatbot.tasks

import software.jevera.gchatbot.OpenAiClient
import software.jevera.gchatbot.util.ContextExtractor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WriteSummaryTask(
    private val openAiClient: OpenAiClient,
    private val contextExtractor: ContextExtractor
) : TaskHandler {
    private val logger = LoggerFactory.getLogger(WriteSummaryTask::class.java)
    override val key: String = "writeSummary"

    override fun run(context: ContextSnapshot): TaskResult {
        val formattedContext = contextExtractor.buildContextString(context)
        val prompt = buildString {
            appendLine("You produce concise summaries of Google Chat conversations.")
            appendLine("Context:")
            append(formattedContext)
        }
        val response = openAiClient.summarize(prompt)
        val summary = openAiClient.firstText(response) ?: run {
            logger.warn("OpenAI returned no summary; falling back to placeholder")
            "I could not generate a summary right now."
        }
        return TaskResult(text = summary)
    }
}
