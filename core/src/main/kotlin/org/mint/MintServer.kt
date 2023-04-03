package org.mint

import org.mint.lib.SUTState

interface MintServer<S : SUTState<S>> {
    val repository: StateRepository<S>
}
