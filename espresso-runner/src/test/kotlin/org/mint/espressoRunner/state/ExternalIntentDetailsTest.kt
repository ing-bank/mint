package com.ing.mint.espressoRunner.state

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.test.espresso.intent.VerifiableIntent
import com.ing.mint.android.xml.attribute
import junit.framework.Assert.assertEquals
import org.hamcrest.Matcher
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

internal class ExternalIntentDetailsTest {
    private val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    private val node: Element = doc.createElement("Action")
    private val externalIntentDetails = ExternalIntentDetails(node)
    private val matcher: Matcher<Intent> = mock()
    private val verifiableIntent: VerifiableIntent = mock()

    @Test
    fun internalIntent() {
        val intent: Intent = mock()
        whenever(verifiableIntent.intent).thenReturn(intent)
        whenever(matcher.matches(intent)).thenReturn(false)

        externalIntentDetails.verify(matcher, listOf(verifiableIntent).toMutableList())

        assertEquals("", node.getAttribute("launchesExternalIntent"))
    }

    @Test
    fun externalIntent() {
        val categoryAppBrowser = Intent.CATEGORY_APP_BROWSER
        val actionInsert = ACTION_VIEW
        val mimeType = "vnd.android.cursor.dir/event"

        val intent: Intent = mock()
        whenever(intent.categories).thenReturn(setOf(categoryAppBrowser))
        whenever(intent.action).thenReturn(actionInsert)
        whenever(intent.type).thenReturn(mimeType)

        whenever(verifiableIntent.intent).thenReturn(intent)
        whenever(matcher.matches(intent)).thenReturn(true)

        externalIntentDetails.verify(matcher, listOf(verifiableIntent).toMutableList())

        assertEquals("true", node.getAttribute("launchesExternalIntent"))
        val intentNode = node.firstChild
        assertEquals(actionInsert, intentNode.attribute("action"))
        assertEquals(mimeType, intentNode.attribute("type"))
        assertEquals(categoryAppBrowser, intentNode.firstChild.firstChild.attribute("name"))
    }
}
