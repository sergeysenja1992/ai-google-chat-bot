package software.jevera.gchatbot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Minimal set of Google Chat event models required by the bot. They are not exhaustive but
 * capture the fields that are relevant for the routing and task execution flow.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GoogleChatEvent(
    val type: String?,
    val eventTime: String?,
    val eventId: String?,
    val message: EventMessage?,
    val space: Space?,
    val user: User?,
    val action: Action?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventMessage(
    val name: String?,
    val sender: User?,
    val text: String?,
    val thread: Thread?,
    val argumentText: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Thread(
    val name: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Space(
    val name: String?,
    val type: String?,
    val threaded: Boolean? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class User(
    val name: String?,
    val displayName: String?,
    val type: String?,
    val domainId: String? = null,
    val avatarUrl: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Action(
    val actionMethodName: String?,
    val parameters: List<ActionParameter>?,
    val data: Map<String, Any>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActionParameter(
    val key: String?,
    val value: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ChatMessage(
    val name: String?,
    val text: String?,
    val createTime: String?,
    val senderDisplayName: String?,
    val senderEmail: String?,
    val threadName: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ChatResponse(
    val text: String? = null,
    val cardsV2: List<CardV2>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CardV2(
    val cardId: String,
    val card: Card
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Card(
    val header: CardHeader? = null,
    val sections: List<CardSection>? = null,
    val cardActions: List<CardAction>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CardHeader(
    val title: String? = null,
    val subtitle: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CardSection(
    val header: String? = null,
    val widgets: List<CardWidget>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed interface CardWidget {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class TextParagraph(val text: String) : CardWidget

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class DecoratedText(
        val text: String,
        val startIcon: Icon? = null,
        @JsonProperty("bottomLabel") val bottomLabel: String? = null
    ) : CardWidget

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ButtonList(val buttons: List<Button>) : CardWidget
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Icon(val knownIcon: String)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Button(
    val text: String,
    val onClick: OnClick
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OnClick(
    val action: CardActionOnClick? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CardActionOnClick(
    val actionMethodName: String,
    val parameters: List<ActionParameter>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CardAction(
    val actionLabel: String,
    val onClick: OnClick
)
