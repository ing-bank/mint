package com.ing.mint.tooling.android

import org.gradle.api.logging.Logger
import java.io.BufferedReader
import java.io.InputStream

object ADBCommand {
    sealed interface ADBResult {
        /** All stdout text of a command that has been executed, irregardless of failure */
        val stdout: String
        val exitCode: Int
        val isSuccess: Boolean
    }
    data class ADBFailure(override val stdout: String, val stderr: String, val msg: String, override val exitCode: Int) : ADBResult {
        override val isSuccess: Boolean = false
    }

    data class ADBSuccess(override val stdout: String, override val exitCode: Int) : ADBResult {
        override val isSuccess: Boolean = true
    }

    /** Execute an ADB command, logging any results to the provided logger */
    fun exec(cmd: String, logger: Logger): Boolean = when (val result = blockingExecute(cmd)) {
        is ADBFailure -> {
            logger.error("Failure: {} (exitCode: {})", result.msg, result.exitCode)
            logger.debug("stdout: {}", result.stdout)
            logger.debug("stderr: {}", result.stderr)
            false
        }
        is ADBSuccess -> {
            logger.quiet("Executed `{}` successfully.", cmd, result.exitCode)
            logger.debug("stdout: {}", result.stdout)
            true
        }
    }

    /** Execute an ADB command, blocking until it finishes execution */
    internal fun blockingExecute(cmd: String): ADBResult {
        val lc: Process = Runtime.getRuntime().exec(cmd)
        lc.waitFor()
        val exitCode = lc.exitValue()
        try {
            return if (exitCode != 0) {
                ADBFailure(
                    collectStream(lc.inputStream),
                    collectStream(lc.errorStream),
                    "Command `$cmd` failed (no device connected?)",
                    exitCode,
                )
            } else {
                ADBSuccess(
                    collectStream(lc.inputStream),
                    exitCode,
                )
            }
        } catch (e: Exception) {
            return ADBFailure(
                "n/a",
                "n/a",
                "An exception was thrown: ${e.message}",
                exitCode,
            )
        }
    }

    private fun collectStream(i: InputStream): String {
        val reader = i.bufferedReader()
        val txt = reader.use(BufferedReader::readText)
        reader.close()
        return txt
    }
}
