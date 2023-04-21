package org.mint.tooling.android

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class CollectReportingDataTask : DefaultTask() {
    @get:Input
    @Optional
    @set: Option(
        option = "target",
        description = "the location of the MINT results",
    )
    var targetDir: String? = null

    @get:Input
    lateinit var packageName: String

    @get:Input
    @Optional
    var timestamp: Long? = null

    @TaskAction
    fun pullResources() {
        val destination = targetDir?.let { project.file(it) }
            ?: File(project.buildDir.absolutePath + "/mint-reports")
        destination.mkdirs()
        CollectReportingData(logger).pullResources(packageName, destination, timestamp)
    }
}
