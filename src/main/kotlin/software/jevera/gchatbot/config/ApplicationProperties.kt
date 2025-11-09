package software.jevera.gchatbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application-properties")
data class ApplicationProperties(
    val openai: OpenAi = OpenAi(),
    val google: Google = Google(),
    val pendingAction: PendingAction = PendingAction(),
    val context: Context = Context()
) {
    data class OpenAi(
        val apiKey: String = "",
        val baseUrl: String = "https://api.openai.com/v1",
        val routerModel: String = "gpt-4.1-mini",
        val summaryModel: String = "gpt-4.1-mini"
    )

    data class Google(
        val chat: Chat = Chat()
    ) {
        data class Chat(
            val verification: String = "",
            val botToken: String = "",
            val baseUrl: String = "https://chat.googleapis.com/v1"
        )
    }

    data class PendingAction(
        val ttlSeconds: Long = 900
    )

    data class Context(
        val threadLimit: Int = 20,
        val roomLimit: Int = 30,
        val maxChars: Int = 8000
    )
}
