package com.ing.mint.android

import org.junit.Before

abstract class StateTest : StateBuilder {
    protected lateinit var state: AndroidState
    protected abstract val widgetTreeXML: String

    @Before
    fun setupXML() {
        state = buildState(widgetTreeXML)
    }
}
