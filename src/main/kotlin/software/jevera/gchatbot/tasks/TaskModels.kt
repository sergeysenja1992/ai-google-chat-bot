package software.jevera.gchatbot.tasks

import software.jevera.gchatbot.ChatMessage
import java.time.Instant

/** Models for task routing and execution. */
data class TaskSpec(val key: String, val description: String)

data class ProposedTask(
    val key: String,
    val confidence: Double,
    val rationale: String,
    val contextPreview: String
)

data class ContextSnapshot(
    val spaceId: String,
    val threadId: String?,
    val threadMessages: List<ChatMessage>,
    val recentRoomMessages: List<ChatMessage>,
    val userLocale: String?,
    val userTimezone: String?
)

data class PendingAction(
    val id: String,
    val proposedTask: ProposedTask,
    val context: ContextSnapshot,
    val createdAt: Instant,
    val expiresAt: Instant
)

data class TaskResult(
    val text: String,
    val card: software.jevera.gchatbot.CardV2? = null
)

interface TaskHandler {
    val key: String
    fun run(context: ContextSnapshot): TaskResult
}
