package org.mint.espressoRunner.state

import android.content.Intent
import androidx.test.espresso.intent.VerifiableIntent
import androidx.test.espresso.intent.VerificationMode
import org.hamcrest.Matcher
import org.w3c.dom.Element

/**
 * Adds intent info to the action that launched the external intent
 */
class ExternalIntentDetails(val node: Element) : VerificationMode {
    override fun verify(
        externalIntentMatcher: Matcher<Intent>,
        recordedIntents: MutableList<VerifiableIntent>?,
    ) {
        for (verifiableIntent in recordedIntents!!) {
            val intent = verifiableIntent.intent
            val isExternalIntent = externalIntentMatcher.matches(intent)
            if (isExternalIntent && !verifiableIntent.hasBeenVerified()) {
                node.setAttribute("launchesExternalIntent", isExternalIntent.toString())

                val doc = node.ownerDocument
                val intentNode = doc.createElement("ExternalIntent")
                intent.type?.let { intentNode.setAttribute("type", it) }
                intent.data?.let { intentNode.setAttribute("data", it.toString()) }
                intent.action?.let { intentNode.setAttribute("action", it) }

                intent.categories?.let { set ->
                    val intentCategoriesNode = doc.createElement("Categories")
                    for (i in set.indices) {
                        set.elementAt(i)?.let {
                            val categoryNode = doc.createElement("Category")
                            categoryNode.setAttribute("name", it)
                            intentCategoriesNode.appendChild(categoryNode)
                        }
                    }
                    intentNode.appendChild(intentCategoriesNode)
                }
                node.appendChild(intentNode)
                verifiableIntent.markAsVerified()
            }
        }
    }
}
