package software.jevera.gchatbot.util

object Logging {
    fun redact(value: String?, keep: Int = 4): String {
        if (value.isNullOrBlank()) return "<empty>"
        return if (value.length <= keep) "***" else value.take(keep) + "***"
    }
}
