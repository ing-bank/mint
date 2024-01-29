package com.ing.mint.api

import com.ing.mint.MINT
import com.ing.mint.StateRepository
import com.ing.mint.android.AndroidConstants
import com.ing.mint.android.AndroidState
import com.ing.mint.android.AndroidStateUtils
import com.ing.mint.android.ApplicationMonitor
import com.ing.mint.android.StateBuilder
import com.ing.mint.android.rule.BasicRules
import com.ing.mint.android.rule.XQueryPredicate
import com.ing.mint.android.rule.input.GenericInputRule
import com.ing.mint.android.xml.hasNS
import com.ing.mint.android.xml.query
import com.ing.mint.android.xml.setAttr
import com.ing.mint.integrationTestRunner.ITTestLoop
import com.ing.mint.util.Either
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.robolectric.RobolectricTestRunner
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory

@RunWith(RobolectricTestRunner::class)
class MintIntegrationTest : StateBuilder {
    private val stepOne: AndroidState = buildState(
        """
        <View>
          <View id="a" isClickable="true" isDisplayed="true"/>
          <View id="b" isClickable="false" isDisplayed="true"/>
        </View>
        """.trimIndent(),
    )

    private val stepTwo: AndroidState = buildState(
        stepOne,
        """
        <View>
          <View id="a" isClickable="true" isDisplayed="true"/>
          <View id="b" isClickable="true" isDisplayed="true"/>
        </View>
        """.trimIndent(),
    )

    private val mint: MINT = MINT.Builder()
        .withRule(BasicRules.simpleClickableRule())
        .withOracle(NoopOracle())
        .withNumberOfSteps(2)
        .withLoopBuilder { ctx, _, _ ->
            ITTestLoop(
                ITTestLoop
                    .builder(ctx)
                    .withStep(stepOne)
                    .withStep(stepTwo)
                    .withInvariant(XQueryPredicate("count(//View) = 3"))
                    .withPredicate(1, XQueryPredicate("count(//action:click[@selected = 'true']) = 1"))
                    .withPredicate(2, XQueryPredicate("count(//action:click[@selected = 'true']) = 1")),
            )
        }
        .build { e -> Assert.fail(e) }!!

    @Test
    fun MINTIntegrationTest() {
        mint.explore()
    }

    @Test
    fun invalidNumberOfSteps() {
        val e = Assert.assertThrows(IllegalStateException::class.java) {
            MINT.Builder()
                .withNumberOfSteps(42)
                .build()
        }

        val reason = "The exception message doesn't match"
        assertThat(reason, e.message, containsString("Invalid number of steps defined"))
    }

    @Test
    fun tooManySteps() {
        val m: MINT? = MINT.DefaultBuilder
            .withNumberOfSteps(2)
            .build()

        // Two steps should be OK
        m?.step {}?.step {}
        val e = Assert.assertThrows(IllegalStateException::class.java) {
            // But the third should not be
            m?.step { }
        }

        val reason = "The exception message doesn't match"
        assertThat(reason, e.message, containsString("Cannot configure an additional step; this is manual step"))
    }

    @Test
    fun invalidNumberOfSequences() {
        val e = Assert.assertThrows(IllegalStateException::class.java) {
            MINT.Builder()
                .withNumberOfSequences(200)
                .build()
        }

        val reason = "The exception message doesn't match"
        assertThat(reason, e.message, containsString("Invalid number of sequences defined"))
    }

    @Test
    fun noActionSelected() {
        val state: AndroidState = buildState(
            """
        <View>
          <View id="a" isClickable="false" />
        </View>
            """.trimIndent(),
        )

        val mint = MINT.Builder()
            .withRule(BasicRules.simpleClickableRule())
            .withOracle(NoopOracle())
            .withNumberOfSteps(1)
            .withLoopBuilder { ctx, _, _ ->
                ITTestLoop(
                    ITTestLoop
                        .builder(ctx)
                        .withStep(state)
                        .withPredicate(1, XQueryPredicate("count(//action:click[@selected = 'true']) = 0"))
                        .withPredicate(
                            1,
                            XQueryPredicate(
                                "count(*:action[@message = " +
                                    "'Invalid SUT: No actionable items found. " +
                                    "Please check if screen has valid widgets that MINT can interact with.']) = 1",
                            ),
                        ),
                )
            }
            .build { e -> Assert.fail(e) }!!

        val e = Assert.assertThrows(IllegalStateException::class.java) {
            mint.explore()
        }
        assertThat(
            "exception message",
            e.message,
            containsString(
                "Invalid SUT: No actionable items found. " +
                    "Please check if screen has valid widgets that MINT can interact with",
            ),
        )
    }

    @Test
    fun multipleActionsSelected() {
        val state: AndroidState = buildState(
            """
        <View>
          <View id="a" isClickable="true" isDisplayed = "true" />
          <View id="a" isClickable="true" isDisplayed = "true" />
        </View>
            """.trimIndent(),
        )

        val mint = MINT.Builder()
            .withRule(BasicRules.simpleClickableRule())
            .withOracle(NoopOracle())
            .withNumberOfSteps(1)
            .withLoopBuilder { ctx, _, _ ->
                object : ITTestLoop(
                    builder(ctx)
                        .withStep(state)
                        .withPredicate(1, XQueryPredicate("count(//action:click[@selected = 'true']) = 2")),
                ) {
                    override fun actionSelection(state: AndroidState): Either<AndroidState, AndroidState> {
                        state.query { it.hasNS(AndroidConstants.RULE_NS) }
                            .flatMap { rg -> rg.query { it.hasNS(AndroidConstants.ACTION_NS) } }
                            .forEach { it.setAttr("selected", "true") }
                        return Either.Right(state)
                    }
                }
            }
            .build { e -> Assert.fail(e) }!!

        val e = Assert.assertThrows(AssertionError::class.java) {
            mint.explore()
        }

        assertThat(
            "error message",
            e.message,
            containsString(
                "Multiple actions were selected in the same step, though only one is expected",
            ),
        )
    }

    @Test
    fun xalanTransformation() {
        val repo = TestRepository("test-repo")
        val mint = emojiInputMintRun(repo)

        mint.explore()

        // apache xalan escapes multi-byte UTF-8 characters into HTML entities which causes parsing issues when processing the file
        // saxon doesn't have this issue
        val tRenderer: Transformer = run {
            val tt = TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl", null).newTransformer()
            tt.setOutputProperty(OutputKeys.INDENT, "yes")
            tt.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            tt
        }

        assertTrue(repo.getContents(tRenderer).contains("&#55357;&#56420;"))
    }

    @Test
    fun saxonTransformation() {
        val repo = TestRepository("test-repo")
        val mint = emojiInputMintRun(repo)

        mint.explore()

        val tRenderer = AndroidStateUtils.tRenderer
        assertFalse(repo.getContents(tRenderer).contains("&#55357;&#56420;"))
    }

    private fun emojiInputMintRun(repo: StateRepository<AndroidState>): MINT {
        val state: AndroidState = buildState(
            """
        <View>
          <View id="a" isEditText="true" isDisplayed="true" hasOnClickListeners="false"/>
          <View id="b" isClickable="true" isDisplayed="true"/>
        </View>
            """.trimIndent(),
        )

        return MINT.Builder()
            .withRule(
                GenericInputRule(
                    description = "Generate UTF8 text streams for anything accepting text",
                    pred = BasicRules.xpred(".[@isEditText='true']"),
                    prio = BasicRules.defaultPrio,
                    gen = { "\uD83D\uDC64" },
                ),
            )
            .withStateRepository(repo)
            .withOracle(NoopOracle())
            .withNumberOfSteps(1)
            .withLoopBuilder { ctx, _, _ ->
                ITTestLoop(
                    ITTestLoop
                        .builder(ctx)
                        .withStep(state)
                        .withPredicate(1, XQueryPredicate("count(//action:input[@selected = 'true']) = 1")),
                )
            }
            .build { e -> Assert.fail(e) }!!
    }

    @Test
    fun monitorLifecycle() {
        val state: AndroidState = buildState(
            """
        <View>
          <View id="a" isClickable="true" />
        </View>
            """.trimIndent(),
        )
        val am: ApplicationMonitor<Nothing> = mock()

        val mint = MINT.Builder()
            .withRule(BasicRules.simpleClickableRule())
            .withOracle(NoopOracle())
            .withNumberOfSteps(1)
            .withApplicationMonitors(setOf(am))
            .withLoopBuilder { ctx, _, _ ->
                ITTestLoop(
                    ITTestLoop
                        .builder(ctx)
                        .withStep(state),
                )
            }
            .build { e -> Assert.fail(e) }!!

        verifyNoInteractions(am)

        mint.start()
        verify(am).initialize()

        mint.stop()
        verify(am).tearDown()
    }
}
