package org.mint.tooling.android

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.mint.tooling.android.CleanDeviceStateTask.CleanDeviceStateTask.cmd

open class CleanDeviceStateTask : DefaultTask() {

    @get:Input
    lateinit var packageName: String

    @TaskAction
    fun clean() {
        ADBCommand.exec("$cmd $packageName", logger)
    }

    object CleanDeviceStateTask {
        const val cmd = "adb shell pm clear"
    }
}
