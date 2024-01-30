package com.ing.mint.android

import org.w3c.dom.Element
import org.w3c.dom.Node

/** An example state abstraction mechanism.
 * Note that this might be slightly dependent on Espresso characteristics,
 * but we are not sure at this time. If it is, it's about the attributes such as 'resourceName'
 * that are presumed to exist in the xml
 */
object ExampleStateAbstraction : AndroidConstants {
    fun mapper(): (Node) -> Node? = { x ->
        if (x is Element) {
            if (x.hasAttribute("resourceName") && x.namespaceURI == null) {
                val doc = x.ownerDocument
                val e = doc.createElementNS(AndroidConstants.ABSTRACT_NS, "state")
                e.setAttribute("resourceName", x.getAttribute("resourceName"))

                e
            } else {
                null
            }
        } else {
            null
        }
    }
}
