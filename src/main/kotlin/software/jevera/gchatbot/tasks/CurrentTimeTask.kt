package software.jevera.gchatbot.tasks

import software.jevera.gchatbot.util.TimezoneResolver
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Component
class CurrentTimeTask(
    private val timezoneResolver: TimezoneResolver
) : TaskHandler {
    override val key: String = "currentTime"

    override fun run(context: ContextSnapshot): TaskResult {
        val zone = timezoneResolver.resolve(context)
        val now = ZonedDateTime.now(zone)
        val formatted = now.format(DateTimeFormatter.RFC_1123_DATE_TIME)
        val text = "Current time (${zone.id}): $formatted"
        return TaskResult(text = text)
    }
}
