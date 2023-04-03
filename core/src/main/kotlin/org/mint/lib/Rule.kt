package org.mint.lib

import java.math.BigDecimal

interface Rule<S : SUTState<S>> {
    /** A description of this rule: what effect does it have? */
    val description: String

    /** The priority of this rule. The higher, the more important something is expected to be */
    fun priority(): (state: S) -> BigDecimal

    /** Apply this rule to a given state */
    fun apply(): (state: S) -> S? // create and extend sub-state
}
