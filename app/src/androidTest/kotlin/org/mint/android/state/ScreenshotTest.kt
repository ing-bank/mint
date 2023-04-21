package org.mint.android.state

import android.os.Environment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import junit.framework.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mint.android.AndroidStateUtils
import org.mint.espressoRunner.state.Screenshot
import org.mint.testapp.TestActivity
import org.w3c.dom.Node
import java.io.File
import java.io.FileOutputStream
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class ScreenshotTest {
    @Rule
    @JvmField
    var activityScenarioRule = ActivityScenarioRule(TestActivity::class.java)

    @Test
    fun screenshotImage() {
        // wait until the views are ready before taking the screenshot
        onView(isRoot()).check(matches(isDisplayed()))

        val doc = AndroidStateUtils.factory.newDocumentBuilder().newDocument()

        val screenshot = Screenshot.toNode(doc)
        val cdataSection: Node = screenshot.firstChild
        var data: String? = null
        when (cdataSection) {
            is org.w3c.dom.CharacterData -> data = cdataSection.data
        }
        data?.let {
            saveToDevice(android.util.Base64.decode(data, 0))
        }
            ?: run {
                fail("No screenshot found!")
            }
    }

    private fun saveToDevice(bytes: ByteArray) {
        val screenshotFolder =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "screenshots",
            )
        if (!screenshotFolder.exists()) {
            screenshotFolder.mkdirs()
        }
        val id = UUID.randomUUID()
        val file = File(screenshotFolder, "screenshot_$id.png")

        val fo = FileOutputStream(file)
        fo.write(bytes)
        fo.flush()
        fo.close()

        println("Saved screenshot as: $file")
    }
}
