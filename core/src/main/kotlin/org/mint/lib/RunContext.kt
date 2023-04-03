package org.mint.lib

/**
 * Run Context of a Mint Loop. Is available for the whole duration of the MINT execution
 * should be IMMUTABLE for its whole lifetime */
interface RunContext<S : SUTState<S>> {
    /** The oracles configured for this scenario. They define what we want to observe/interpret */
    val oracles: Set<Oracle<S>>

    /** The rules that are applied to the SUT state every loop */
    val rules: Set<Rule<S>>
}
