package org.mint.lib

/** core Oracle. They are composable and consists of queries on the SUTState to search for problematic versions.  */
interface Oracle<S : SUTState<S>> {
    val name: String
    val version: String
    val description: String
    val categories: Set<OracleCategory>

    /** Which probe(s) does this oracle depend on for it to be able to form a judgement? */
    fun probes(): Set<Class<out Probe<S>>>

    /** Evaluate the given state
     *
     * This will pull in additional information from the defined probes as needed.
     * Any verdicts will be added to the state, before it will be returned.
     * */
    fun eval(state: S): S
}
