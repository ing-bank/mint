package com.ing.mint

import com.ing.mint.lib.SUTState

interface MintServer<S : SUTState<S>> {
    val repository: StateRepository<S>
}
