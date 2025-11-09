package software.jevera.gchatbot

import software.jevera.gchatbot.cards.ConfirmationCard
import software.jevera.gchatbot.state.PendingActionStore
import software.jevera.gchatbot.tasks.ContextSnapshot
import software.jevera.gchatbot.tasks.TaskCatalog
import software.jevera.gchatbot.tasks.TaskHandler
import software.jevera.gchatbot.tasks.TaskSelector
import software.jevera.gchatbot.util.ContextExtractor
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/events")
class BotController(
    private val objectMapper: ObjectMapper,
    private val signatureVerifier: SignatureVerifier,
    private val contextExtractor: ContextExtractor,
    private val taskSelector: TaskSelector,
    private val pendingActionStore: PendingActionStore,
    handlers: List<TaskHandler>,
    private val taskCatalog: TaskCatalog
) {
    private val logger = LoggerFactory.getLogger(BotController::class.java)
    private val handlerByKey = handlers.associateBy { it.key }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun handleEvent(@RequestBody body: String, request: HttpServletRequest): ResponseEntity<Any> {
        signatureVerifier.verify(request.getHeader("X-Goog-Chat-Signature"), body)
        val event = objectMapper.readValue(body, GoogleChatEvent::class.java)
        logger.info("Received event type={} space={} thread={}", event.type, event.space?.name, event.message?.thread?.name)
        return when (event.type) {
            "ADDED_TO_SPACE" -> handleAddedToSpace(event)
            "MESSAGE" -> handleMessage(event)
            "CARD_CLICKED" -> handleCardAction(event)
            else -> ResponseEntity.ok(mapOf("text" to "Unsupported event."))
        }
    }

    private fun handleAddedToSpace(event: GoogleChatEvent): ResponseEntity<Any> {
        val text = "Thanks for adding me! I'll help route tasks based on the conversation context."
        return ResponseEntity.ok(mapOf("text" to text))
    }

    private fun handleMessage(event: GoogleChatEvent): ResponseEntity<Any> {
        val contextSnapshot = captureContext(event)
        val proposedTask = taskSelector.proposeTask(contextSnapshot)
        val pending = pendingActionStore.create(proposedTask, contextSnapshot)
        val card = ConfirmationCard.build(pending)
        val response = ChatResponse(cardsV2 = listOf(card))
        return ResponseEntity.ok(response)
    }

    private fun handleCardAction(event: GoogleChatEvent): ResponseEntity<Any> {
        val actionName = event.action?.actionMethodName
        val actionId = event.action?.parameters?.firstOrNull { it.key == "actionId" }?.value
        if (actionId.isNullOrBlank()) {
            logger.warn("CARD_CLICKED without actionId")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("text" to "Missing action id."))
        }
        val pending = pendingActionStore.get(actionId)
        if (pending == null) {
            return ResponseEntity.ok(mapOf("text" to "That request has expired. Please try again."))
        }
        return when (actionName) {
            "confirm_run" -> runTask(pending)
            "decline_run" -> {
                pendingActionStore.remove(actionId)
                val alternatives = taskCatalog.tasks.joinToString(", ") { it.key }
                ResponseEntity.ok(mapOf("text" to "Okay! Available tasks: $alternatives"))
            }
            else -> ResponseEntity.ok(mapOf("text" to "Action not recognized."))
        }
    }

    private fun captureContext(event: GoogleChatEvent): ContextSnapshot = try {
        contextExtractor.snapshot(event)
    } catch (ex: Exception) {
        logger.error("Failed to capture context: {}", ex.message)
        ContextSnapshot(
            spaceId = event.space?.name ?: "unknown",
            threadId = event.message?.thread?.name,
            threadMessages = emptyList(),
            recentRoomMessages = emptyList(),
            userLocale = null,
            userTimezone = null
        )
    }

    private fun runTask(pending: software.jevera.gchatbot.tasks.PendingAction): ResponseEntity<Any> {
        val handler = handlerByKey[pending.proposedTask.key]
        if (handler == null) {
            pendingActionStore.remove(pending.id)
            logger.warn("No handler for task {}", pending.proposedTask.key)
            return ResponseEntity.ok(mapOf("text" to "I don't know how to run ${pending.proposedTask.key} yet."))
        }
        val result = handler.run(pending.context)
        pendingActionStore.remove(pending.id)
        val response = if (result.card != null) {
            ChatResponse(text = result.text, cardsV2 = listOf(result.card))
        } else {
            ChatResponse(text = result.text)
        }
        return ResponseEntity.ok(response)
    }
}
