package com.ing.mint.api

import com.ing.mint.StateRepository
import com.ing.mint.android.AndroidState
import com.ing.mint.android.AndroidStateUtils
import com.ing.mint.android.InMemoryRepository
import com.ing.mint.android.StateBuilder
import com.ing.mint.lib.Query
import javax.xml.transform.Transformer

/**
 * A test repository that allows inspecting the persisted state.
 */
class TestRepository(name: String) : StateRepository<AndroidState>, StateBuilder {
    private val inMemoryRepository = InMemoryRepository(name)
    private var state: AndroidState? = null

    override fun correlate(state: AndroidState): AndroidState = inMemoryRepository.correlate(state)

    override fun query(query: Query<AndroidState>): Set<AndroidState> = inMemoryRepository.query(query)

    override fun persist(state: AndroidState): AndroidState {
        persistAndroidState(state)
        return inMemoryRepository.persist(state)
    }

    private fun persistAndroidState(state: AndroidState) {
        this.state = state
    }

    fun getContents(transformer: Transformer): String {
        return AndroidStateUtils.renderXML(state!!.node, transformer)
    }
}
