package com.ing.mint

import com.ing.mint.android.AndroidConstants
import com.ing.mint.android.AndroidCtx
import com.ing.mint.android.AndroidCtxImpl
import com.ing.mint.android.AndroidState
import com.ing.mint.android.ApplicationMonitor
import com.ing.mint.android.ExampleActionAbstraction
import com.ing.mint.android.ExampleStateAbstraction
import com.ing.mint.android.InMemoryRepository
import com.ing.mint.android.NaivePersistentRepository
import com.ing.mint.android.TestMetadata
import com.ing.mint.android.oracle.AndroidDeviceOracle
import com.ing.mint.android.oracle.AndroidLogOracle
import com.ing.mint.android.oracle.CrashOracle
import com.ing.mint.android.oracle.accessibility.AccessibilityOracles
import com.ing.mint.android.rule.BaseRule
import com.ing.mint.android.rule.BasicRules
import com.ing.mint.android.rule.dialog.BottomSheetRules
import com.ing.mint.android.rule.input.GenericInputRule
import com.ing.mint.android.rule.input.datetime.PickerInputRules
import com.ing.mint.android.rule.viewgroup.AdapterViewRules
import com.ing.mint.android.rule.viewgroup.ViewGroupRules
import com.ing.mint.espressoRunner.EspressoFailureMonitor
import com.ing.mint.espressoRunner.EspressoLoop
import com.ing.mint.lib.Oracle
import com.ing.mint.lib.Rule
import com.ing.mint.util.Either
import com.ing.mint.util.orNull
import com.ing.mint.util.zip
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

/** Configure a MINT run.
 *
 * Note: you probably want to use the [MINTRule] abstraction and the [MINT.Builder] instead
 * of directly using the constructor of this class.
 *
 * Given that the lifetime of a MINT class is governed by the MINTRule, we can safely use it to
 * keep track of the sequence number and inject it in the loop builder. **/
class MINT private constructor(
    private val ctx: AndroidCtx,
    private val numberOfSteps: Int,
    private val numberOfSequences: Int,
    private val loopBuilder: (AndroidCtx, String, TestMetadata?) -> MintLoop<AndroidState>,
) : MINTApi, AndroidConstants {

    private var currentSequence: String = ""
    private val actions: MutableList<() -> Unit> = mutableListOf()
    private var testMetadata: TestMetadata? = null

    companion object {
        // Sensible min/max values of sequence/step count.
        // This prevents the user from setting silly values
        private const val defaultNumberOfSequences = 3
        private const val defaultNumberOfSteps = 25
        private const val maxNumberOfSequences = 100
        private const val maxNumberOfSteps = 40

        // Note: we might want to get this from a config file or environment eventually?
        private const val DEFAULT_REPOSITORY_NAME = "mint-state-data"

        val DefaultBuilder by lazy {
            Builder()
                .withStateRepository(NaivePersistentRepository.namedInstance(DEFAULT_REPOSITORY_NAME))
                .withDefaultRules()
                .withDefaultOracles()
                .withNumberOfSequences(defaultNumberOfSequences)
                .withNumberOfSteps(defaultNumberOfSteps)
        }

        /** A default MINT instance. Use this when you immediately want to get started. */
        val Default by lazy {
            DefaultBuilder
                .build()!!
        }
    }

    /**
     * Create a MINT Builder instance if you want to configure the MINT tool in detail
     */
    data class Builder(
        private var repository: StateRepository<AndroidState> = InMemoryRepository(),
        private var abstraction: (Node) -> Node? = ExampleStateAbstraction.mapper(),
        private var action_abstraction: (Node) -> Node? = ExampleActionAbstraction.mapper(),
        private var numberOfSteps: Int = defaultNumberOfSteps,
        private var numberOfSequences: Int = defaultNumberOfSequences,
        private var rules: Set<Rule<AndroidState>> = setOf(),
        private var oracles: Set<Oracle<AndroidState>> = setOf(),
        private var loopBuilder: (AndroidCtx, String, TestMetadata?) -> MintLoop<AndroidState> = { ctx, seq, metadata -> EspressoLoop(ctx, seq, metadata) },
        private var applicationMonitors: Set<ApplicationMonitor<*>> = setOf(EspressoFailureMonitor.instance),
    ) {

        /** What state repository will be used to store application state? */
        fun withStateRepository(repository: StateRepository<AndroidState>) = apply { this.repository = repository }

        /** Which mapper is used to do state abstractions? */
        fun withStateAbstraction(abstraction: (Node) -> Node?) = apply { this.abstraction = abstraction }

        /** Define the number of steps that need to be taken before we stop exploring the application */
        fun withNumberOfSteps(steps: Int) = apply { this.numberOfSteps = steps }

        /** Define the number of sequences we do per test (i.e. serial executions ) */
        fun withNumberOfSequences(sequences: Int) = apply { this.numberOfSequences = sequences }

        /** Add a rule to this MINT configuration */
        fun withRule(rule: Rule<AndroidState>) = apply { this.rules = this.rules + rule }

        /** Enable all the default rules for convenience */
        fun withDefaultRules() = apply {
            val actionRules: Set<BaseRule> = setOf(
                BasicRules.scrollingClickableRule(),
                BasicRules.clickableRuleForItemWithTag(),
                BasicRules.clickableRuleBasedOnPositionInViewHierarchy(),
                BasicRules.clickableRuleBasedOnPositionInViewHierarchyForPopupItem(),
                BasicRules.deprioritizeClickingOnPopupItemOnCurrentRoot(),
                BasicRules.deviceRotationRule(),
                BasicRules.deviceThemeRule(),
                AdapterViewRules.clickableRuleForAdapterViewItems(),
                AdapterViewRules.clickableRuleForSpinnerItems(),
                AdapterViewRules.spinnerSimpleClickDeprioritizeRule(),
                AdapterViewRules.adapterViewSimpleClickDeprioritizeRule(),
                ViewGroupRules.scrollingPagerRightRule(),
                ViewGroupRules.scrollingPagerLeftRule(),
                PickerInputRules.timePickerInputRule(),
                PickerInputRules.datePickerInputRule(),
                BottomSheetRules.clickableRuleBasedOnPositionInViewHierarchy(),
            ) + GenericInputRule.rules +
                GenericInputRule.deprioritizationRules

            val historicalActionDeprioritizationRules =
                BasicRules.defaultHistoricalActionsDeprioritizeRules(actionRules)

            this.rules = this.rules + actionRules + historicalActionDeprioritizationRules
        }

        /** Add an oracle to this MINT configuration */
        fun withOracle(oracle: Oracle<AndroidState>) = apply { this.oracles = this.oracles + oracle }

        fun withoutOracle(oracle: Oracle<AndroidState>) = apply { this.oracles = this.oracles.filterNot { it.name == oracle.name }.toSet() }

        /** Enable all the default oracles for convenience */
        fun withDefaultOracles() = apply {
            this.oracles = this.oracles + setOf(
                AndroidLogOracle(),
                AndroidDeviceOracle(),
                CrashOracle(),
            ) + AccessibilityOracles.all
        }

        /** Provide a loop builder that creates a fresh mint loop instance according to the provided confdiguration */
        fun withLoopBuilder(loopBuilder: (AndroidCtx, String, TestMetadata?) -> MintLoop<AndroidState>) = apply { this.loopBuilder = loopBuilder }

        fun withApplicationMonitors(am: Set<ApplicationMonitor<*>>) = apply { this.applicationMonitors = am }

        /** Build a MINT instance that can be used for testing */
        fun build(errorCallback: (String) -> Unit = { e -> throw IllegalStateException(e) }): MINT? {
            return validateSteps().zip(
                validateSequences(),
                validateRules(),
                validateOracles(),
            ) { steps, sequences, rules, oracles ->

                val cfgBuilder = { doc: Document ->
                    val cfg = doc.createElement("config")

                    fun withItem(name: String, key: String, value: String): Element {
                        val el = doc.createElement(name)
                        el.setAttribute(key, value)
                        return el
                    }

                    fun withItems(parentName: String, childName: String, children: Iterable<Map<String, String>>): Element {
                        val el = doc.createElement(parentName)
                        for (child in children) {
                            val c = doc.createElement(childName)
                            for ((k, v) in child) {
                                c.setAttribute(k, v)
                            }
                            el.appendChild(c)
                        }
                        return el
                    }

                    cfg.appendChild(withItem("repository", "type", repository.javaClass.canonicalName ?: "unknown"))

                    // TODO: probably abstraction and action_abstraction should become
                    // well known types with descriptions and versions, just like the oracles

                    cfg.appendChild(
                        withItems(
                            "rules",
                            "rule",
                            rules.map {
                                mapOf(
                                    Pair("name", it.javaClass.canonicalName ?: "unknown"),
                                    Pair("description", it.description),
                                )
                            },
                        ),
                    )

                    cfg.appendChild(
                        withItems(
                            "oracles",
                            "oracle",
                            oracles.map {
                                mapOf(
                                    Pair("name", it.javaClass.canonicalName ?: "unknown"),
                                    Pair("description", it.description),
                                    Pair("categories", it.categories.joinToString(", ") { x -> x.name }),
                                )
                            },
                        ),
                    )

                    cfg.appendChild(withItem("steps", "count", steps.toString()))
                    cfg.appendChild(withItem("sequences", "count", sequences.toString()))

                    cfg
                }

                val testMetadataBuilder = {
                        doc: Document, testMetadata: TestMetadata ->
                    val metadataNode = doc.createElement("testMetadata")

                    metadataNode.setAttribute("name", testMetadata.methodName)
                    metadataNode.setAttribute("class", testMetadata.className)
                    metadataNode.setAttribute("id", testMetadata.id)

                    metadataNode
                }

                val ctx = AndroidCtxImpl(
                    repository,
                    abstraction,
                    action_abstraction,
                    cfgBuilder,
                    testMetadataBuilder,
                    rules,
                    oracles,
                    applicationMonitors,
                )
                MINT(
                    ctx,
                    steps,
                    sequences,
                    loopBuilder,
                )
            }
                .tapLeft { e ->
                    errorCallback(e.message)
                }
                .orNull()
        }

        private fun validateSteps(): Either<MINTConfigError, Int> = if (numberOfSteps <= 0 || numberOfSteps > maxNumberOfSteps) {
            Either.Left(MINTConfigError.InvalidNumberOfSteps(numberOfSteps, maxNumberOfSteps))
        } else {
            Either.Right(numberOfSteps)
        }

        private fun validateSequences(): Either<MINTConfigError, Int> = if (numberOfSequences <= 0 || numberOfSequences > maxNumberOfSequences) {
            Either.Left(MINTConfigError.InvalidNumberOfSequences(numberOfSequences, maxNumberOfSequences))
        } else {
            Either.Right(numberOfSequences)
        }

        private fun validateRules(): Either<MINTConfigError, Set<Rule<AndroidState>>> = if (rules.isEmpty()) {
            Either.Left(MINTConfigError.NoRulesDefined)
        } else {
            Either.Right(rules)
        }

        private fun validateOracles(): Either<MINTConfigError, Set<Oracle<AndroidState>>> = if (rules.isEmpty()) {
            Either.Left(MINTConfigError.NoOraclesDefined)
        } else {
            Either.Right(oracles)
        }
    }

    override fun step(action: () -> Unit): MINT {
        if (actions.size >= numberOfSteps) {
            throw java.lang.IllegalStateException("Cannot configure an additional step; this is manual step # ${actions.size + 1}, but $numberOfSteps is the allowed maximum. You might want to increase this.")
        }
        actions.add(action)
        return this
    }

    /** Explore the SUT based on the provided configuration. */
    override fun explore() {
        val loop = loopBuilder(ctx, currentSequence, testMetadata)
        loop.before()
        // ensure we always invoke the after() as promised in our interface
        try {
            // If we registered any manual step(s) execute them first in the context of this mint config
            actions.forEach {
                loop.step(it)
            }
            // For the remainder of steps, execute
            for (i in (actions.size + 1)..numberOfSteps) {
                loop.step(null)
            }
        } finally {
            // Invoke after as promised
            loop.after()
            // Also clear the actions that have been registered manually
            actions.clear()
        }
    }

    fun numberOfSequences(): Int = numberOfSequences

    /** When using the MINT class directly, please ensure to call this before invoking explore() */
    fun start() {
        ctx.applicationMonitors.forEach { it.initialize() }
    }

    /** When using the MINT class directly, please ensure to call this after invoking explore() */
    fun stop() {
        ctx.applicationMonitors.forEach { it.tearDown() }
        testMetadata = null
    }

    fun updateTestMetadata(testMetadata: TestMetadata) {
        this.testMetadata = testMetadata
    }

    fun currentSequence(seq: String) {
        currentSequence = seq
    }
}

sealed class MINTConfigError {
    abstract val message: String
    data class InvalidNumberOfSteps(val steps: Int, val maxNumberOfSteps: Int) : MINTConfigError() {
        override val message = "Invalid number of steps defined: must be at least 1, maximum $maxNumberOfSteps, but $steps given"
    }
    data class InvalidNumberOfSequences(val sequences: Int, val maxNumberOfSequences: Int) : MINTConfigError() {
        override val message = "Invalid number of sequences defined: must be at least 1, maximum $maxNumberOfSequences, but $sequences given"
    }
    object NoRulesDefined : MINTConfigError() {
        override val message = "No rules have been configured. Try to use `withDefaultRules` or `withRule` when setting up MINT"
    }
    object NoOraclesDefined : MINTConfigError() {
        override val message = "No oracles have been configured. Try to use `withDefaultOracles` or `withOracle` when setting up MINT"
    }
}
