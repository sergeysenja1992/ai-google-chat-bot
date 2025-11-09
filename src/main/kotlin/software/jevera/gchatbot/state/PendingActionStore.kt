package software.jevera.gchatbot.state

import software.jevera.gchatbot.tasks.PendingAction
import software.jevera.gchatbot.tasks.ProposedTask
import software.jevera.gchatbot.tasks.ContextSnapshot
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class PendingActionStore(
    @Value("\${pending-action.ttl-seconds:900}") ttlSeconds: Long
) {
    private val logger = LoggerFactory.getLogger(PendingActionStore::class.java)
    private val ttl = Duration.ofSeconds(ttlSeconds)
    private val actions = ConcurrentHashMap<String, PendingAction>()

    fun create(proposedTask: ProposedTask, context: ContextSnapshot): PendingAction {
        val id = UUID.randomUUID().toString()
        val now = Instant.now()
        val action = PendingAction(
            id = id,
            proposedTask = proposedTask,
            context = context,
            createdAt = now,
            expiresAt = now.plus(ttl)
        )
        actions[id] = action
        logger.debug("Stored pending action {} for space {} thread {}", id, context.spaceId, context.threadId)
        return action
    }

    fun get(id: String): PendingAction? {
        val action = actions[id]
        if (action != null && action.expiresAt.isBefore(Instant.now())) {
            logger.info("Pending action {} expired; deleting", id)
            actions.remove(id)
            return null
        }
        return action
    }

    fun remove(id: String) {
        actions.remove(id)
    }

    fun purgeExpired() {
        val now = Instant.now()
        actions.entries.removeIf { (_, action) -> action.expiresAt.isBefore(now) }
    }
}
