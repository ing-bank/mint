package com.ing.mint.android

import com.ing.mint.android.xml.attribute
import com.ing.mint.android.xml.extend
import com.ing.mint.android.xml.hasAttribute
import com.ing.mint.android.xml.hasNS
import com.ing.mint.android.xml.notCorrelated
import com.ing.mint.android.xml.parent
import com.ing.mint.android.xml.query
import com.ing.mint.android.xml.setAttr
import com.ing.mint.lib.Rule
import com.ing.mint.lib.SUTState
import com.ing.mint.util.Either
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.math.BigDecimal

data class AndroidState(val node: Node, val rnd: java.util.Random) : SUTState<AndroidState> {
    fun copy(): AndroidState {
        // make sure to reuse the random object as to be really random
        return derive(AndroidStateUtils.copy(node))
    }

    /** Given a selection, select a random item from that selection
     *
     * Using this method ensures the centrally seeded Random object is used, as to be repeatable
     */
    fun <T> selectOneOf(items: Collection<T>): T =
        items.elementAt(rnd.nextInt(items.size))

    /** Derive a new AndroidState from this one, containing on a new node */
    fun derive(node: Node): AndroidState =
        AndroidState(node, rnd)

    override fun children(): List<AndroidState> {
        return if (node.hasChildNodes()) {
            val children = node.childNodes
            (0 until children.length).map { derive(children.item(it)) }.toList()
        } else {
            listOf()
        }
    }

    /**
     * Places rule-group nodes directly under View nodes
     * We need this to group rules/actions for each View for convenient action selection
     */
    fun extendWithRuleGroups() {
        this.node.extend {
            if (it.namespaceURI == null && it is Element && "View" == it.nodeName) {
                it.ownerDocument.createElementNS(AndroidConstants.RULE_NS, "rule-group")
            } else {
                null
            }
        }
    }

    /** Apply a rule recursively to this _mutable_ state (within a rule-group) */
    fun apply(rule: Rule<AndroidState>) {
        extend { x: Node ->
            if (x.hasNS(AndroidConstants.RULE_NS) && "rule-group" == x.nodeName) {
                rule.apply()(derive(x.parent()))?.node
            } else {
                null
            }
        }
    }

    /** Walk the tree and apply `f` to all nodes, mutating the tree in place */
    fun extend(f: (Node) -> Node?): Unit = node.extend(f)

    /** Query the tree based on a given predicate */
    fun query(p: (Node) -> Boolean): List<Node> = node.query(p)

    /**
     * Calculate the cryptographic hash of an action abstraction, and its parent action abstraction,
     * recursively up to root (the abstract action 'path')
     *
     **/
    fun createParentHashes() = createParentHashes(node)

    /** Generate a cryptographic hash in base36 representing the identity of the AndroidState, resulting in a stable naming scheme */
    fun filenameHash(): String = AndroidStateUtils.toBase36(
        AndroidStateUtils.hash(
            AndroidStateUtils.renderXML(node).encodeToByteArray(),
        ),
    )

    /** Create an element related to the owner document of the this state. It is not attached to the document */
    fun createElement(name: String, namespace: String? = null): Element {
        return if (namespace == null) {
            node.ownerDocument.createElement(name)
        } else {
            node.ownerDocument.createElementNS(namespace, name)
        }
    }

    /** Attach a new child Element to the current node embedded in this AndroidState */
    fun appendChildNode(name: String, namespace: String? = null, block: (e: Element) -> Unit = {}): Element {
        val child = createElement(name, namespace)
        block(child)
        node.appendChild(child)
        return child
    }

    private fun createParentHashes(n: Node, hash: ByteArray = byteArrayOf()) {
        var nhash = hash

        if (n.namespaceURI != null && n is Element) {
            if (n.namespaceURI.equals(AndroidConstants.ABSTRACT_ACTION_NS)) {
                nhash = AndroidStateUtils.hash(AndroidStateUtils.renderXML(n).toByteArray() + hash)
                n.setAttribute("self-and-parents-hash", AndroidStateUtils.toBase64(nhash))
            }
        }
        if (n.hasChildNodes()) {
            val children = n.childNodes
            for (i in 0 until children.length) {
                createParentHashes(children.item(i), nhash)
            }
        }
    }

    /**
     * Action selection is based on (relative) priorities that are provided by (a set of) actions,
     * optionally modified by modifier actions that can de-prioritize or prioritize them,
     * One could consider action selection to be the 'lower cortex brain' of MINT.
     *
     **/

    fun selectAction(): Either<AndroidState, AndroidState> {
        val actionSelection = this
        // get the rule-groups
        val ruleGroups = actionSelection.query {
            it.hasNS(AndroidConstants.RULE_NS) &&
                "rule-group" == it.nodeName &&
                it.notCorrelated()
        }

        // group actions contained by the rule-groups on their nodeName (i.e. click, input)
        val actionTypesGroupedPerRuleGroup: List<List<Node>> = ruleGroups.map { r ->
            r.query { it.hasNS(AndroidConstants.ACTION_NS) && it.notCorrelated() }
                .groupBy { it.nodeName }
                .map { it.value }
        }.flatten()

        // determine a list of actions and their derived priorities (with modifiers applied)
        val actions: List<Pair<BigDecimal, Node>> = actionTypesGroupedPerRuleGroup.map { a ->
            // split the actions into modify actions and regular actions
            val (modifiers, regular) = a.partition { it.hasAttribute(AndroidConstants.MODIFIER) }

            // Associate each action priority (i.e. the derived priority) with itself
            var pRegular = regular.map { o ->
                Pair(BigDecimal(o.attribute(AndroidConstants.PRIORITY)), o)
            }

            // Apply and map all modifier actions over all regular actions
            modifiers.forEach { m ->
                when (m.attribute(AndroidConstants.MODIFIER)) {
                    AndroidConstants.MULTIPLICATIVE -> {
                        pRegular = pRegular.map { p ->
                            val prio = BigDecimal(m.attribute(AndroidConstants.PRIORITY))
                            Pair(p.first * prio, p.second)
                        }
                    }
                    else -> throw RuntimeException("UNKNOWN MODIFIER")
                }
            }

            // Copy the derived priority into an action attribute
            for (p in pRegular) { p.second.setAttr("derived-priority", "${p.first}") }

            pRegular
        }.flatten()

        return randomlySelect(actionSelection, actions)
    }

    private fun randomlySelect(state: AndroidState, actions: List<Pair<BigDecimal, Node>>): Either<AndroidState, AndroidState> {
        // take the total sum of derived priorities
        val sum = actions.map { it.first }.sumOf { it }
        // throw a dice
        val dice = sum * BigDecimal(rnd.nextFloat().toDouble())

        var total = BigDecimal(0)

        // Do a weighted selection of an action
        for (a in actions) {
            total += a.first
            if (total >= dice) {
                // indicate that it is selected
                a.second.setAttr("selected", "true")
                return Either.Right(state)
            }
        }
        // If there are no actions, we still succeed and make the analysis at the end
        // decide whether or not that's a failure.
        return Either.Right(state)
    }

    override fun toString() = "AndroidState(node=${AndroidStateUtils.renderXML(node)})"
}
