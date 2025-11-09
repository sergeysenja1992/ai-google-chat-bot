package software.jevera.gchatbot.cards

import software.jevera.gchatbot.ActionParameter
import software.jevera.gchatbot.Button
import software.jevera.gchatbot.Card
import software.jevera.gchatbot.CardActionOnClick
import software.jevera.gchatbot.CardHeader
import software.jevera.gchatbot.CardSection
import software.jevera.gchatbot.CardV2
import software.jevera.gchatbot.CardWidget
import software.jevera.gchatbot.Icon
import software.jevera.gchatbot.OnClick
import software.jevera.gchatbot.tasks.PendingAction

object ConfirmationCard {
    private const val CARD_ID = "confirm-task"

    fun build(action: PendingAction): CardV2 {
        val task = action.proposedTask
        val widgets = listOf(
            CardWidget.DecoratedText(
                text = "Proposed task: <b>${task.key}</b>",
                startIcon = Icon("CHECK_CIRCLE")
            ),
            CardWidget.TextParagraph("<b>Rationale:</b><br/>${task.rationale}"),
            CardWidget.TextParagraph("<b>Context preview:</b><br/><font size=\"-1\">${task.contextPreview}</font>"),
            CardWidget.ButtonList(
                buttons = listOf(
                    Button(
                        text = "Run it",
                        onClick = OnClick(
                            action = CardActionOnClick(
                                actionMethodName = "confirm_run",
                                parameters = listOf(ActionParameter("actionId", action.id))
                            )
                        )
                    ),
                    Button(
                        text = "Pick another",
                        onClick = OnClick(
                            action = CardActionOnClick(
                                actionMethodName = "decline_run",
                                parameters = listOf(ActionParameter("actionId", action.id))
                            )
                        )
                    )
                )
            )
        )
        return CardV2(
            cardId = CARD_ID,
            card = Card(
                header = CardHeader(title = "Run task?", subtitle = "${task.key} (confidence ${(task.confidence * 100).toInt()}%)"),
                sections = listOf(CardSection(widgets = widgets))
            )
        )
    }
}
