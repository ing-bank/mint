package org.mint.android

import org.mint.StateRepository
import org.mint.android.query.AnyQuery
import org.mint.android.xml.attribute
import org.mint.android.xml.children
import org.mint.android.xml.hasNS
import org.mint.android.xml.notCorrelated
import org.mint.lib.Query
import org.mint.util.MapUtil.getOrDefaultExt
import org.w3c.dom.Node

/*
 * A basic in-memory Repository
 *
 * Keeps and correlates all (abstract and concrete) historical states and actions, in-memory
 *
 * TODO: implement query
 */
data class InMemoryRepository(val name: String = "repo") : StateRepository<AndroidState> {

    // Maps abstractId to a set of concrete EspressoStates
    var abstractState = mapOf<String, Set<AndroidState>>()

    // Maps abstractId to a map of actionId to rule/action EspressoStates fragments
    var abstractActions: Map<String, Map<String, List<AndroidState>>> = mapOf<String, Map<String, List<AndroidState>>>()

    override fun correlate(state: AndroidState): AndroidState {
        val id = abstractID(state)
        val actions = abstractActions.getOrDefaultExt(id, mapOf())

        // Correlate previous actions for this abstract state (if any)
        if (actions.isNotEmpty()) {
            state.extend {
                if (it.hasNS(AndroidConstants.ABSTRACT_ACTION_NS)) {
                    val hash = it.attribute("self-and-parents-hash")
                    val a = actions.getOrDefault(hash, listOf())

                    // Correlate historical actions for an abstract action (id)
                    if (a.isNotEmpty()) {
                        val doc = it.ownerDocument
                        val corr = doc.createElementNS(AndroidConstants.CORRELATE_NS, "historical")

                        // append/insert them into an extension node
                        a.forEach { val n = doc.importNode(it.node, true); corr.appendChild(n) }

                        corr
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }

        return state
    }

    override fun query(query: Query<AndroidState>): Set<AndroidState> {
        if (query is AnyQuery) {
            return abstractState.map { it.value }
                .flatten().map {
                    it
                }
                .toSet()
        } else {
            throw RuntimeException("Only AnyQuery is supported for InMemoryRepository")
        }
    }

    /* Find the 'nearest' self-and-parents-hash attribute, which should be sibling node */
    fun getNearestAbstractActionParentsHash(n: Node): String? {
        if (n.parentNode == null) {
            return null
        } else if (n.parentNode == n.ownerDocument) {
            return null
        } else {
            val parent = n.parentNode

            // query siblings for the 'self-and-parents-hash' attribute
            val abstractActions = parent.children()
                .filter { it != n }
                .filter { it.hasNS(AndroidConstants.ABSTRACT_ACTION_NS) }
                .mapNotNull { it.attribute("self-and-parents-hash") }

            return when (abstractActions.size) {
                0 -> getNearestAbstractActionParentsHash(n.parentNode) // not found, recurse up
                1 -> abstractActions.first() // found it
                else -> throw RuntimeException("TODO") // TODO: May be concatenate the hashes?
            }
        }
    }

    /* Determine the selected abstract action id, correlated with the concrete rule/action */
    fun abstractAction(state: AndroidState): Pair<String, AndroidState>? {
        val selectedAction = state.query {
            it.hasNS(AndroidConstants.ACTION_NS) &&
                ("true" == it.attribute("selected")) &&
                it.notCorrelated()
        }
        if (selectedAction.size == 1) {
            val action = selectedAction.first()
            var parentHash = getNearestAbstractActionParentsHash(action)
            if (parentHash == null) {
                // Worst case we don't know everything, so now it all collapses to
                // the default generic hash -- the empty string
                parentHash = ""
            }

            return Pair(parentHash, state.derive(action.parentNode))
        } else {
            return null
        }
    }

    /* Determine the abstract id, which is the hash all abstract nodes, concatenated */
    fun abstractID(state: AndroidState): String {
        val abstract = state.query {
            it.hasNS(AndroidConstants.ABSTRACT_NS)
        }
        // Serialize all abstract nodes to 1 big string (ordered)
        val abstractStrings = (abstract.map { x -> AndroidStateUtils.renderXML(x) }).joinToString("")

        return AndroidStateUtils.toBase64(AndroidStateUtils.hash(abstractStrings.toByteArray()))
    }

    /* Store the state, and update in-memory 'database' */
    override fun persist(state: AndroidState): AndroidState {
        val id = abstractID(state)
        val action = abstractAction(state)

        /* update abstract state */
        val set = abstractState.getOrDefaultExt(id, setOf())
        abstractState = abstractState + Pair(id, set + state)

        /* update abstract actions */
        val map = abstractActions.getOrDefaultExt(id, mapOf())
        action?.let {
            val list = map.getOrDefaultExt(action.first, listOf())
            abstractActions =
                abstractActions + Pair(id, map + Pair(action.first, list + action.second))
        }

        // TODO: add 'persisted tag' to root node?
        return state
    }
}
