package com.ing.mint.lib

/** SUTState is hierarchical */
interface SUTState<S : SUTState<S>> {
    fun children(): List<S>
}
