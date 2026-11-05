package com.navtasks.app

/**
 * Deleting a template must not leave orphaned dated runs. Keep this calculation
 * pure so full deletion can be tested without touching a phone's preferences.
 */
internal data class PermanentlyDeletedTask(
    val templates: List<TaskTemplate>,
    val runs: Map<String, TaskRun>,
)

internal fun taskStopRepeating(task: TaskTemplate): TaskTemplate = task.copy(enabled = false)

internal fun taskDeletePermanently(
    templates: List<TaskTemplate>,
    runs: Map<String, TaskRun>,
    taskId: String,
): PermanentlyDeletedTask? {
    if (templates.none { it.id == taskId }) return null
    return PermanentlyDeletedTask(
        templates = templates.filterNot { it.id == taskId },
        runs = runs.filterValues { it.taskId != taskId },
    )
}
