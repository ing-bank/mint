package com.ing.mint.tooling.android

import org.gradle.api.logging.Logger
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class CollectReportingData(private val logger: Logger) {

    fun pullResources(packageName: String, destination: File?, notBefore: Long?): File? {
        val target = destination ?: createTempDirectory("mint-")
        val mintPath = "/storage/emulated/0/Android/data/$packageName/files/Documents/mint"

        notBefore?.let {
            val d = SimpleDateFormat("yyyy-MM-ddHH:mm:ss").format(Date(notBefore))
            val f = ADBCommand.blockingExecute("adb shell find $mintPath -type f -newermt '$d'")
            if (!f.isSuccess || f.stdout.isBlank()) {
                logger.quiet("No files to pull")
                return null
            }
        }
        // maintain timestamp of transferred files
        val cmd = "adb pull -a $mintPath ${target.absolutePath}"
        return if (ADBCommand.exec(cmd, logger)) {
            target
        } else {
            logger.quiet("Pulled MINT resources to {}", target.absolutePath)
            null
        }
    }

    private fun createTempDirectory(prefx: String): File {
        // nio paths introduced in Android 26+
        val tmp = File.createTempFile(prefx, "")
        tmp.delete()
        tmp.mkdirs()
        return tmp
    }
}
