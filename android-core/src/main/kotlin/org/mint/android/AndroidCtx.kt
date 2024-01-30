package com.ing.mint.android

import com.ing.mint.StateRepository
import com.ing.mint.lib.Oracle
import com.ing.mint.lib.Rule
import com.ing.mint.lib.RunContext
import org.w3c.dom.Document
import org.w3c.dom.Node

/** Interface because data classes cannot really be extended */
interface AndroidCtx : RunContext<AndroidState> {
    val repository: StateRepository<AndroidState>
    val abstract: (Node) -> Node?
    val action_abstraction: (Node) -> Node?
    val config: (Document) -> Node
    val metadata: (Document, TestMetadata) -> Node
    override val rules: Set<Rule<AndroidState>>
    override val oracles: Set<Oracle<AndroidState>>
    val applicationMonitors: Set<ApplicationMonitor<*>>
}

data class AndroidCtxImpl(
    override val repository: StateRepository<AndroidState>,
    override val abstract: (Node) -> Node?,
    override val action_abstraction: (Node) -> Node?,
    override val config: (Document) -> Node,
    override val metadata: (Document, TestMetadata) -> Node,
    override val rules: Set<Rule<AndroidState>>,
    override val oracles: Set<Oracle<AndroidState>>,
    override val applicationMonitors: Set<ApplicationMonitor<*>>,
) : AndroidCtx
