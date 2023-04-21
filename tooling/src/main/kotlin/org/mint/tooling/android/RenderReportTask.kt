package org.mint.tooling.android

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.mint.android.NaivePersistentRepository
import org.mint.tooling.android.reporting.ReferenceReportRenderer
import org.mint.tooling.android.reporting.ReportRenderer
import java.io.File

open class RenderReportTask : DefaultTask() {
    @get:Input
    lateinit var packageName: String

    @get: Input
    @set: Option(
        option = "no-pull",
        description = "indicates to skip data retrieval from the device\\n" +
            "and to use local MINT data found at the default or specified location",
    )
    var noPull = false

    @get:Input
    @Optional
    @set: Option(
        option = "target",
        description = "the location of the MINT results",
    )
    var targetDir: String? = null

    private var renderer: ReportRenderer? = null

    @TaskAction
    fun renderReport() {
        val mintTarget = targetDir?.let { project.file(it) }
            ?: File(project.buildDir.absolutePath + "/mint-reports")

        if (noPull) {
            logger.quiet("Skipping the retrieval of MINT data from the device")
        } else {
            mintTarget.mkdirs()

            val crd = CollectReportingData(logger)
            crd.pullResources(packageName, mintTarget, null)
                ?: throw IllegalStateException("Cannot retrieve resources from device, abandoning report generation")
        }

        mintTarget.resolve("mint").listFiles()?.filter { it.isDirectory }?.forEach {
            logger.quiet("Loading and unifying repository {} in {}", it.name, mintTarget.absolutePath)
            // Note: this repository is pulled from the 'libs' folder because we cannot
            // share android and jvm modules between each other easily. Therefore there are two
            // gradle tasks in 'android-core' and 'core' that copy the jars with the classes to our
            // local 'libs' folder so this class can be resolved. In case of issues, try to reload
            // gradle, your editor might be confused by this workaround
            NaivePersistentRepository(it.name, it).serializeToReport()
        }

        // TODO: Hardcode the implementation for now
        renderer = ReferenceReportRenderer(logger)

        createReports(mintTarget)
    }

    /** Generate HTML reports for all of the XML reports in all of the database locations */
    private fun createReports(target: File) {
        val dirs = target.resolve("mint").listFiles()?.filter { it.isDirectory }
        dirs?.forEach {
            createReport(it)
        }
    }

    /** For each of the XML reports, create an HTML report */
    private fun createReport(target: File) {
        val xmls = target.listFiles {
                _, name ->
            name?.lowercase()?.endsWith(".xml") ?: false
        }

        if (renderer == null) {
            logger.error("No report renderer defined")
        } else {
            logger.quiet("Using report renderer: ${renderer?.javaClass?.canonicalName}")

            xmls?.let {
                for (xml in it) {
                    val out = renderer?.render(xml)
                    logger.quiet("Report generated at {}", out?.absolutePath)
                }
            }
        }
    }
}
