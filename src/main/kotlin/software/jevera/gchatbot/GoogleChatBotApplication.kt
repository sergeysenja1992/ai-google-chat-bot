package software.jevera.gchatbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GoogleChatBotApplication

fun main(args: Array<String>) {
    runApplication<GoogleChatBotApplication>(*args)
}
