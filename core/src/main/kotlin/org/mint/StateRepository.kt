package com.ing.mint

import com.ing.mint.lib.Query
import com.ing.mint.lib.SUTState

interface StateRepository<S : SUTState<S>> {
    /** Ask how the current state relates to other persisted data, such as history etc */
    fun correlate(state: S): S

    /** Query the repository for all states satisfying the provided query **/
    fun query(query: Query<S>): Set<S>

    /** Indicate that an action is taken */
    fun persist(state: S): S
}
