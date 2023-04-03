package org.mint

import org.mint.lib.SUTState

interface Reporter<T, S : SUTState<S>> {
    fun report(states: List<S>): T
    fun report(state: S): T = report(listOf(state))
}
