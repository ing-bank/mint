package org.mint.api

import org.mint.StateRepository
import org.mint.android.AndroidState
import org.mint.android.AndroidStateUtils
import org.mint.android.InMemoryRepository
import org.mint.android.StateBuilder
import org.mint.lib.Query
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
