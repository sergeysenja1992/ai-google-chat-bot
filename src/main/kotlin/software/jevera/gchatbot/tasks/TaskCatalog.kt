package software.jevera.gchatbot.tasks

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class TaskCatalog {
    val tasks: List<TaskSpec> by lazy { loadTasks() }

    val byKey: Map<String, TaskSpec> by lazy { tasks.associateBy { it.key } }

    private fun loadTasks(): List<TaskSpec> {
        val resource = ClassPathResource("tasks.yaml")
        if (!resource.exists()) {
            throw IllegalStateException("tasks.yaml not found on classpath")
        }
        val lines = resource.inputStream.bufferedReader().use { it.readLines() }
        val tasks = parseTasks(lines)
        if (tasks.isEmpty()) {
            throw IllegalStateException("Task catalog cannot be empty")
        }
        return tasks
    }

    private fun parseTasks(lines: List<String>): List<TaskSpec> {
        val tasks = mutableListOf<TaskSpec>()
        var currentKey: String? = null
        var currentDescription: String? = null
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("- key:") -> {
                    if (currentKey != null) {
                        tasks += TaskSpec(currentKey!!, currentDescription ?: "")
                        currentDescription = null
                    }
                    currentKey = line.substringAfter(":").trim().trim('"')
                }
                line.startsWith("description:") -> {
                    currentDescription = line.substringAfter(":").trim().trim('"')
                }
            }
        }
        if (currentKey != null) {
            tasks += TaskSpec(currentKey!!, currentDescription ?: "")
        }
        return tasks
    }
}
