package software.jevera.gchatbot.util

import software.jevera.gchatbot.tasks.ContextSnapshot
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class TimezoneResolver {
    fun resolve(snapshot: ContextSnapshot): ZoneId = snapshot.userTimezone
        ?.let {
            runCatching { ZoneId.of(it) }.getOrNull()
        }
        ?: ZoneId.of("UTC")
}
