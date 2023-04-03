package org.mint

import org.junit.Assert.fail
import org.junit.rules.ExternalResource
import org.junit.runner.Description
import org.junit.runners.model.Statement

/** A JUnit rule that encapsulates the [MINT] state.
 *
 * Note that it is configured by providing a [MINT] instance via the accompanied [MINT.Builder]
 *
 * The rule makes sure that we encapsulate a [RepeatableTestStatement] that ensures the # sequences
 * for MINT are properly executed. It also ensures the lifetime of the MINT components are properly managed.
 */
class MINTRule(private val mint: MINT? = org.mint.MINT.Default) : MINTApi, ExternalResource() {
    private val metadataRule = TestMetadataRule()

    override fun apply(stmt: Statement, description: Description): Statement {
        val name = description.displayName
        val sequenceDividerStart = name.lastIndexOf('[')
        val sequenceDividerEnd = name.lastIndexOf(']')
        val noSequence = sequenceDividerStart == -1 || sequenceDividerEnd == -1

        if (mint == null) {
            fail("MINT has not been configured properly, cannot execute the given test")
        }
        if (noSequence) {
            fail("The MintClassRunner is required when using the MintRule, cannot execute the given test")
        }

        val seq = name.substring(sequenceDividerStart + 1, sequenceDividerEnd)
        mint!!.currentSequence(seq)

        return metadataRule.apply(
            super.apply(stmt, description),
            description
        )
    }

    override fun explore() = mint?.explore() ?: Unit

    override fun step(action: () -> Unit): MINTRule {
        mint?.step(action)
        return this
    }

    internal fun numberOfSequences(): Int {
        return mint!!.numberOfSequences()
    }

    override fun before() {
        mint?.start()
        metadataRule.metadata()?.let { mint!!.updateTestMetadata(it) }
    }

    override fun after() {
        mint?.stop()
    }
}
