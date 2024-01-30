package com.ing.mint

import com.ing.mint.lib.RunContext
import com.ing.mint.lib.SUTState

interface MintRun<S : SUTState<S>> {
    fun ctx(): RunContext<S>

    fun apply() {
        setup()
        execute()
        report()
    }

    // setup the env
    fun setup(): Unit

    // do the mint loop
    fun execute(): Unit

    // build a report
    fun report(): Unit
}
