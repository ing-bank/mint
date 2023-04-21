package org.mint.tooling.android

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.VerificationTask

class ToolingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("mintTooling", ToolingExtension::class.java)
        val packageNameCheck = {
            if (extension.packageName.isNullOrBlank()) {
                throw IllegalStateException("Invalid packageName, it cannot be empty")
            }
        }
        val targetDirCheck = {
            if (extension.targetDir?.isBlank() == true) {
                throw IllegalStateException("Invalid targetDir, it cannot be empty")
            }
        }

        project.afterEvaluate {
            // find the android instrumented test tasks
            project.tasks.matching {
                    task ->
                val defaultInstrumentationTaskClass =
                    "com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask"

                task is VerificationTask &&
                    task.javaClass.canonicalName.contains(defaultInstrumentationTaskClass)
            }.forEach {
                    task ->
                task.finalizedBy(COLLECT_REPORTING_DATA_TASK)
                val collectReportingDataTask =
                    project.tasks.findByName(COLLECT_REPORTING_DATA_TASK)
                val junitReportEnrichingTask =
                    project.tasks.findByName(JUNIT_REPORT_ENRICHING_TASK)
                collectReportingDataTask?.finalizedBy(JUNIT_REPORT_ENRICHING_TASK)

                task.doFirst {
                    // todo account for time differences between the device on which the instrumented test is executing
                    //  and the machine running the gradle tasks
                    val testStartTime = System.currentTimeMillis()
                    if (task.hasProperty("resultsDir")) {
                        task.property("resultsDir")?.let { any ->
                            (any as DirectoryProperty?)?.let {
                                collectReportingDataTask?.setProperty(
                                    "targetDir",
                                    it.get().asFile.toString(),
                                )
                                junitReportEnrichingTask?.setProperty(
                                    "junitResults",
                                    it.get().asFile.toString(),
                                )
                                junitReportEnrichingTask?.setProperty(
                                    "mintResults",
                                    it.get().asFile.resolve("mint").toString(),
                                )
                            }
                        }
                        val testData = (task.property("testData") as Property<*>?)?.get()
                        (
                            testData?.javaClass
                                ?.getMethod("getTestedApplicationId")
                                ?.invoke(testData) as Property<*>?
                            )?.get()
                            ?.let {
                                collectReportingDataTask?.setProperty("packageName", it)
                            }
                        collectReportingDataTask?.setProperty("timestamp", testStartTime)
                        junitReportEnrichingTask?.setProperty("timestamp", testStartTime)
                    }
                }
            }
        }

        project.tasks.register(MINT_CLEAN_TASK, CleanDeviceStateTask::class.java) {
                task ->
            packageNameCheck.invoke()
            task.packageName = extension.packageName!!
        }
        project.tasks.register(MINT_REPORT_TASK, RenderReportTask::class.java) {
                task ->
            packageNameCheck.invoke()
            task.packageName = extension.packageName!!
            targetDirCheck.invoke()
            task.targetDir = extension.targetDir
            // if plugin is applied to top-level project, don't run the instrumented tests
            if (project.projectDir.equals(project.rootDir)) {
                project.logger.quiet("Skipping the execution of instrumented tests")
            } else {
                task.dependsOn("connectedDebugAndroidTest")
            }
        }
        project.tasks.register(COLLECT_REPORTING_DATA_TASK, CollectReportingDataTask::class.java) {
                task ->
            packageNameCheck.invoke()
            targetDirCheck.invoke()
            task.packageName = extension.packageName!!
            task.targetDir = extension.targetDir
        }
        project.tasks.register(
            JUNIT_REPORT_ENRICHING_TASK,
            JunitReportEnrichingTask::class.java,
        )
    }

    companion object {
        private const val JUNIT_REPORT_ENRICHING_TASK = "junitReportEnriching"
        private const val COLLECT_REPORTING_DATA_TASK = "collectReportingData"
        private const val MINT_CLEAN_TASK = "mintClean"
        private const val MINT_REPORT_TASK = "mintReport"
    }
}
