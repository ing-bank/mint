package org.mint.android.xml

import org.mint.android.AndroidConstants
import org.w3c.dom.Node

/**
 * Attaches information to the supplied node about the encountered throwable .
 */
object ThrowableAttributes {

    fun apply(node: Node, throwable: Throwable) {
        throwable::class.java.canonicalName?.let { node.setAttr(AndroidConstants.CLASS, it) }
        throwable.message?.let { node.setAttr(AndroidConstants.ERROR_MESSAGE, it) }
        val truncatedStacktrace = node.ownerDocument
            .createCDATASection(
                throwable.stackTrace
                    .take(5)
                    .joinToString(separator = "\nat ", prefix = "at ")
            )
        node.appendChild(truncatedStacktrace)

        throwable.cause?.let {
            val cause = node.ownerDocument.createElement("cause")
            node.appendChild(cause)
            this.apply(cause, it)
        }
    }
}
