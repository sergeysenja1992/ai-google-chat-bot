package software.jevera.gchatbot.cards

import software.jevera.gchatbot.Card
import software.jevera.gchatbot.CardHeader
import software.jevera.gchatbot.CardSection
import software.jevera.gchatbot.CardV2
import software.jevera.gchatbot.CardWidget

object ResultCard {
    fun build(title: String, body: String): CardV2 = CardV2(
        cardId = "task-result",
        card = Card(
            header = CardHeader(title = title),
            sections = listOf(
                CardSection(
                    widgets = listOf(
                        CardWidget.TextParagraph(body)
                    )
                )
            )
        )
    )
}
