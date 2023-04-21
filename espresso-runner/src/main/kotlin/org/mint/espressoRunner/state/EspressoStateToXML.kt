package org.mint.espressoRunner.state

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import org.mint.android.AndroidStateUtils
import org.mint.espressoRunner.state.attributes.ViewAttributes
import org.mint.espressoRunner.state.attributes.ViewGroupAttributes
import org.mint.espressoRunner.state.attributes.WindowAttributes
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

object EspressoStateToXML {
    /** Create an empty document representing the SUT */
    fun emptySUT(): Pair<Document, Element> {
        val doc = AndroidStateUtils.newDocument()
        val root = doc.createElement("SystemUnderTest")
        doc.adoptNode(root)
        return Pair(doc, root)
    }

    fun toXML(roots: List<View>): Element {
        val (doc, root) = emptySUT()
        root.appendChild(DeviceInfo.toNode(doc))
        root.appendChild(Screenshot.toNode(doc))

        val applicationNode = doc.createElement("Application")
        root.appendChild(applicationNode)
        AppInfo.toNode(doc)?.let { applicationNode.appendChild(it) }
        applicationNode.appendChild(AppComponents.toNode(doc))

        roots.forEach { view ->
            val windowNode = doc.createElement("Window")
            WindowAttributes.apply((windowNode as Element), view)
            val viewHierarchy = toXML(doc, view, "root")
            windowNode.appendChild(viewHierarchy)
            applicationNode.appendChild(windowNode)
        }

        return root
    }

    fun toString(n: Node): String = AndroidStateUtils.toXdm(n).toString()

    private fun toXML(doc: Document, view: View, positionInViewHierarchy: String): Node {
        val viewElement = doc.createElement("View")

        // Apply Attributes
        applyResourceAttributes(viewElement, view, positionInViewHierarchy)
        applyDetailedViewAttributes(viewElement, view)
        applyWindowAttributes(viewElement, view)

        // Add Accessibility Check Findings
        AccessibilityViewCheck.apply(viewElement, view)

        // Recursion - for each child in the ViewGroup
        if (view is ViewGroup) {
            ViewGroupAttributes.apply(viewElement, view)
            for (i in 0 until view.childCount) {
                viewElement.appendChild(
                    toXML(
                        doc,
                        view.getChildAt(i),
                        positionInViewHierarchy.plus(".$i"),
                    ),
                )
            }
        }
        return viewElement
    }

    /**
     * Apply attributes from the view resources (if applicable)
     */
    private fun applyResourceAttributes(
        viewElement: Element,
        view: View,
        positionInViewHierarchy: String,
    ) {
        viewElement.setUserData("origin", view, null)
        viewElement.setAttribute("positionInViewHierarchy", positionInViewHierarchy)
        viewElement.setAttribute("class", view.javaClass.canonicalName)

        val viewResources = view.resources
        val viewId = view.id

        if (viewResources != null && viewId != View.NO_ID) {
            viewElement.setAttribute("id", "" + viewId)
            try {
                viewElement.setAttribute(
                    "resourceName",
                    viewResources.getResourceEntryName(viewId),
                )
                viewElement.setAttribute(
                    "package",
                    viewResources.getResourcePackageName(viewId),
                )
            } catch (e: Resources.NotFoundException) {
                println("Resource with id=$viewId not found")
            }
        }
    }

    /**
     * Apply View Attributes for all views even without an ID or resources. This is to ensure
     * that all views are being included in the view hierarchy for MINT to interact with.
     */
    private fun applyDetailedViewAttributes(
        viewElement: Element,
        view: View,
    ) {
        ViewAttributes.apply(viewElement, view)
    }

    private fun applyWindowAttributes(
        viewElement: Element,
        view: View,
    ) {
        viewElement.setAttribute("hasWindowFocus", view.hasWindowFocus().toString())
    }
}
