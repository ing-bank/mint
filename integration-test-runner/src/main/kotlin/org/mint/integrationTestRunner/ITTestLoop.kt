package org.mint.integrationTestRunner

import org.mint.android.AndroidCtx
import org.mint.android.AndroidLoop
import org.mint.android.AndroidState
import org.mint.android.TestMetadata
import org.mint.util.Either
import org.mint.util.MapUtil.getOrDefaultExt
import org.w3c.dom.Element

open class ITTestLoop(
    private val _ctx: TestCtx,
    m: TestMetadata = TestMetadata("", "", "")
) : AndroidLoop(_ctx, "1", m) {
    private var stepCount = 1

    companion object {
        fun builder(ctx: AndroidCtx): TestCtx {
            return if (ctx is TestCtx) {
                ctx
            } else {
                TestCtx(
                    ctx.repository,
                    ctx.abstract,
                    ctx.action_abstraction,
                    ctx.config,
                    ctx.metadata,
                    ctx.rules,
                    ctx.oracles,
                    ctx.applicationMonitors,
                    listOf(),
                    mapOf(),
                    setOf()
                )
            }
        }
    }

    override fun obtainConcreteState(): Either<AndroidState, AndroidState> {
        val state = _ctx.stepState[stepCount - 1]
        state.node.appendChild(ctx.config(state.node.ownerDocument))
        state.node.appendChild(metadata?.let { ctx.metadata(state.node.ownerDocument, it) })
        return Either.Right(state)
    }

    override fun abstract(state: AndroidState): Either<AndroidState, AndroidState> {
        state.extend(ctx.abstract)
        state.extend(ctx.action_abstraction)
        state.createParentHashes()
        return Either.Right(state)
    }

    override fun perform(n: Element) {
        // noop here
    }

    private fun assertState(state: AndroidState) {
        for (r in _ctx.assertions.getOrDefaultExt(stepCount, setOf())) {
            if (!r.invoke(state)) {
                throw IllegalStateException("Assertion failed on step $stepCount: ${r.xquery}")
            }
        }

        for (r in _ctx.invariants) {
            if (!r.invoke(state)) {
                throw IllegalStateException("Invariant failed on step $stepCount: ${r.xquery}")
            }
        }
    }

    override fun persist(state: AndroidState): Either<AndroidState, AndroidState> {
        assertState(state)
        stepCount++
        return super.persist(state)
    }
}
