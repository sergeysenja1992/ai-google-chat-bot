package software.jevera.gchatbot.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.jevera.gchatbot.ChatMessage
import software.jevera.gchatbot.GoogleChatClient
import software.jevera.gchatbot.GoogleChatEvent
import software.jevera.gchatbot.config.ApplicationProperties
import software.jevera.gchatbot.tasks.ContextSnapshot

@Component
class ContextExtractor(
    private val googleChatClient: GoogleChatClient,
    private val applicationProperties: ApplicationProperties
) {
    private val logger = LoggerFactory.getLogger(ContextExtractor::class.java)
    private val contextProperties = applicationProperties.context
    private val threadLimit = contextProperties.threadLimit
    private val roomLimit = contextProperties.roomLimit
    private val maxChars = contextProperties.maxChars

    fun snapshot(event: GoogleChatEvent): ContextSnapshot {
        val spaceName = event.space?.name ?: error("Space name missing from event")
        val threadName = event.message?.thread?.name
        val threadMessages = googleChatClient.fetchThreadMessages(spaceName, threadName, threadLimit)
        val recentMessages = googleChatClient.fetchRecentSpaceMessages(spaceName, roomLimit)
        logger.debug("Context snapshot for space {} thread {} contains {} thread messages and {} room messages",
            spaceName, threadName, threadMessages.size, recentMessages.size)
        return ContextSnapshot(
            spaceId = spaceName,
            threadId = threadName,
            threadMessages = threadMessages,
            recentRoomMessages = recentMessages,
            userLocale = null,
            userTimezone = null
        )
    }

    fun buildPreview(snapshot: ContextSnapshot, previewChars: Int = 800): String {
        val builder = StringBuilder()
        appendMessages(builder, "Thread", snapshot.threadMessages)
        appendMessages(builder, "Room", snapshot.recentRoomMessages)
        val preview = builder.toString()
        return if (preview.length <= previewChars) preview else preview.take(previewChars)
    }

    fun buildContextString(snapshot: ContextSnapshot): String {
        val builder = StringBuilder()
        appendMessages(builder, "Thread", snapshot.threadMessages)
        appendMessages(builder, "Room", snapshot.recentRoomMessages)
        val value = builder.toString()
        return if (value.length <= maxChars) value else value.take(maxChars)
    }

    private fun appendMessages(builder: StringBuilder, title: String, messages: List<ChatMessage>) {
        if (messages.isEmpty()) {
            return
        }
        builder.appendLine("=== $title Messages ===")
        messages.forEach { message ->
            val author = message.senderDisplayName ?: "Unknown"
            val text = message.text?.trim() ?: ""
            builder.appendLine("[$author] $text")
        }
        builder.appendLine()
    }
}
