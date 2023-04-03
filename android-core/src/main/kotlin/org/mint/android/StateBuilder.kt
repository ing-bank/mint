package org.mint.android

import java.util.*

interface StateBuilder {
    fun buildState(xml: String): AndroidState {
        val doc = AndroidStateUtils.factory.newDocumentBuilder().parse(xml.byteInputStream())
        val tree = doc.documentElement
        return AndroidState(tree, Random())
    }

    fun buildState(state: AndroidState, xml: String): AndroidState {
        val doc = AndroidStateUtils.factory.newDocumentBuilder().parse(xml.byteInputStream())
        val tree = doc.documentElement
        return state.derive(tree)
    }
}
