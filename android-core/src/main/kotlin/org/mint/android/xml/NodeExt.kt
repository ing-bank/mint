package org.mint.android.xml

import org.mint.android.AndroidConstants
import org.w3c.dom.Element
import org.w3c.dom.Node

/** Yield all of the Node's children as a list */
fun Node.children(): List<Node> {
    return if (this.hasChildNodes()) {
        val children = this.childNodes
        List<Node>(children.length) { children.item(it) }
    } else {
        listOf()
    }
}

/** Walk the tree and apply `f` to all nodes, mutating the tree in place */
fun Node.extend(f: (Node) -> Node?) {
    val n = this
    val nn = f(n)

    if (n.hasChildNodes()) {
        val children = n.childNodes
        for (i in 0 until children.length) children.item(i).extend(f)
    }

    if (nn != null) { n.appendChild(nn) }
}

/** Query the tree based on a given predicate */
fun Node.query(predicate: (Node) -> Boolean): List<Node> {
    val n = this

    var l: List<Node> = listOf()

    if (predicate(n)) { l = l + n }

    if (n.hasChildNodes()) {
        n.children().forEach {
            l = l + it.query(predicate)
        }
    }

    return l
}

fun Node.parent(): Node = this.parentNode

fun Node.hasAttribute(name: String): Boolean {
    return if (this is Element) {
        this.hasAttribute(name)
    } else {
        false
    }
}
fun Node.hasNS(namespace: String): Boolean = (namespace == this.namespaceURI)
fun Node.attribute(name: String): String? {
    return if (this is Element) {
        this.getAttribute(name)
    } else {
        null
    }
}
fun Node.setAttr(n: String, v: String): Node? {
    return if (this is Element) { this.setAttribute(n, v) ; this } else null
}

fun Node.notCorrelated(): Boolean {
    return if (this.parentNode == null) {
        true
    } else if (this.parentNode == this.ownerDocument) {
        true
    } else if (AndroidConstants.CORRELATE_NS == this.namespaceURI) {
        false
    } else {
        this.parentNode.notCorrelated()
    }
}

fun Node.appendChild(
    tagName: String,
    namespace: String? = null,
    attributes: List<Pair<String, String>>? = null,
): Node {
    val document = ownerDocument

    val element = document.createElementNS(namespace, tagName)
    attributes?.forEach { pair ->
        element.setAttribute(pair.first, pair.second)
    }

    return appendChild(element)
}
