package com.ing.mint.tooling.android

import com.ing.mint.android.AndroidStateUtils
import com.ing.mint.android.xml.attribute
import com.ing.mint.android.xml.children
import com.ing.mint.android.xml.parent
import com.ing.mint.android.xml.query
import com.ing.mint.tooling.android.reporting.VerdictUtil.countVerdicts
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * Adds verdict information to the test result file,
 * complying with the [junit schema](https://github.com/windyroad/JUnit-Schema/blob/master/JUnit.xsd)
 */
abstract class JunitReportEnrichingTask : DefaultTask() {
    @get:Input
    var junitResults: String? = null

    @get:Input
    var mintResults: String? = null

    @get:Input
    @Optional
    var timestamp: Long? = null

    @TaskAction
    fun generate() {
        val mintResultsDir = mintResults?.let { project.file(it) }

        val mintStateFiles = mintResultsDir
            ?.walk()
            ?.filter { it -> it.name.equals(ABSTRACT_STATES) }
            ?.flatMap { it.walk().filter { f -> f.isFile && f.lastModified() >= timestamp!! } }
            ?.toList()

        if (mintStateFiles?.isEmpty() == true) {
            return
        }

        val statesByTestCase = mintStateFiles?.map {
            val i = AndroidStateUtils.factory.newDocumentBuilder().parse(it.inputStream())
            i.documentElement
        }?.groupBy {
                document ->
            val testMetadata = document.children().find { it.nodeName == TEST_METADATA }
            val testName = testMetadata?.attribute(NAME).orEmpty()
            val className = testMetadata?.attribute(CLASS).orEmpty()
            Pair(testName, className)
        }

        val junitResultsDir = junitResults?.let { project.file(it) }
        val junitResultFiles = junitResultsDir?.listFiles {
                _, name ->
            name?.lowercase()?.endsWith(".xml") ?: false
        }

        junitResultFiles?.forEach {
                file ->
            val doc = AndroidStateUtils.factory.newDocumentBuilder().parse(file.inputStream())

            statesByTestCase?.forEach() {
                val (totalVerdicts, totalNotOkVerdicts) = it.value.toSet().countVerdicts()
                val className = it.key.second
                val testName = it.key.first

                val sessions = it.value.flatMap {
                    it.query {
                        it.nodeName.equals("AndroidLoop")
                    }
                }.map { it.attribute("session") }.distinct()

                // all the states of a test run should have the same session
                assert(sessions.size == 1)

                val t = doc.documentElement.query { node ->
                    node.nodeName.equals(TESTCASE) &&
                        node.attribute(NAME)?.equals(testName) == true &&
                        node.attribute(CLASSNAME)?.equals(className) == true
                }

                t.forEach { testcase ->
                    val testSuite = testcase.parent()
                    val testDetails = "$className $testName ${sessions.first()}"
                    if (totalNotOkVerdicts > 0) {
                        val stderr = doc.createElement(SYSTEM_ERR)
                        val content = doc.createCDATASection("$testDetails $totalNotOkVerdicts NOK/$totalVerdicts total verdicts")
                        stderr.appendChild(content)
                        testSuite.appendChild(stderr)
                    } else {
                        val stdout = doc.createElement(SYSTEM_OUT)
                        val content = doc.createCDATASection("$testDetails 100% OK/$totalVerdicts total verdicts")
                        stdout.appendChild(content)
                        testSuite.appendChild(stdout)
                    }
                }
            }
            FileOutputStream(file.absolutePath).use { fos ->
                OutputStreamWriter(fos, Charsets.UTF_8).use { osw ->
                    osw.write(AndroidStateUtils.renderXML(doc.documentElement))
                }
            }
        }
    }

    companion object {
        private const val TESTCASE = "testcase"
        private const val NAME = "name"
        private const val CLASSNAME = "classname"
        private const val SYSTEM_ERR = "system-err"
        private const val SYSTEM_OUT = "system-out"
        private const val CLASS = "class"
        private const val TEST_METADATA = "testMetadata"
        private const val ABSTRACT_STATES = "abstract_states"
    }
}
