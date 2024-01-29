package com.ing.mint.integrationTestRunner

import com.ing.mint.StateRepository
import com.ing.mint.android.AndroidCtx
import com.ing.mint.android.AndroidState
import com.ing.mint.android.ApplicationMonitor
import com.ing.mint.android.TestMetadata
import com.ing.mint.android.rule.XQueryPredicate
import com.ing.mint.lib.Oracle
import com.ing.mint.lib.Rule
import com.ing.mint.util.MapUtil.getOrDefaultExt
import org.w3c.dom.Document
import org.w3c.dom.Node

data class TestCtx(
    override val repository: StateRepository<AndroidState>,
    override val abstract: (Node) -> Node?,
    override val action_abstraction: (Node) -> Node?,
    override val config: (Document) -> Node,
    override val metadata: (Document, TestMetadata) -> Node,
    override val rules: Set<Rule<AndroidState>>,
    override val oracles: Set<Oracle<AndroidState>>,
    override val applicationMonitors: Set<ApplicationMonitor<*>>,
    val stepState: List<AndroidState>,
    val assertions: Map<Int, Set<XQueryPredicate>>,
    val invariants: Set<XQueryPredicate>,
) : AndroidCtx {
    /** Append the given state to the series of states given to the MINT tool */
    fun withStep(state: AndroidState): TestCtx = copy(
        stepState = stepState + state,
    )

    /** Assign a predicate to a given step. Note that we start counting at 1 (for the first step). */
    fun withPredicate(step: Int, predicate: XQueryPredicate): TestCtx = copy(
        assertions = assertions.plus(Pair(step, assertions.getOrDefaultExt(step, setOf()) + predicate)),
    )
    fun withInvariant(predicate: XQueryPredicate): TestCtx = copy(
        invariants = invariants + predicate,
    )
}
