package com.ing.mint.android

import android.util.Log
import com.ing.mint.MintLoop
import com.ing.mint.android.xml.ThrowableAttributes
import com.ing.mint.android.xml.attribute
import com.ing.mint.android.xml.hasAttribute
import com.ing.mint.android.xml.hasNS
import com.ing.mint.android.xml.notCorrelated
import com.ing.mint.lib.Oracle
import com.ing.mint.lib.Probe
import com.ing.mint.lib.ProbeTimingCategory
import com.ing.mint.lib.Rule
import com.ing.mint.util.Either
import com.ing.mint.util.flatMap
import com.ing.mint.util.redeemWith
import org.w3c.dom.Element
import java.util.UUID

abstract class AndroidLoop(final override val ctx: AndroidCtx, val sequence: String, val metadata: TestMetadata?) : AndroidConstants, MintLoop<AndroidState> {
    // Unique Session ID
    var sessionID: UUID? = null

    // Current step -- which step in the current sequence
    var step = 0

    // eagerly collect all the probes, as the context is immutable anyway
    // Create a single instance of each type, and map them to their class so they can
    // be injected into the probes
    private val probes: Set<Probe<AndroidState>> = run {
        val probeConstructors = ctx
            .oracles
            .map { oracle ->
                oracle.probes()
                    .distinct()
                    .partition { it.constructors.any { c -> c.parameterTypes.contains(Set::class.java) } }
            }

        val simpleProbes = probeConstructors.flatMap { it.second }
            .map { it.newInstance() }
            .toSet()
        val observerProbes = probeConstructors.flatMap { it.first }
            .map { it.getConstructor(Set::class.java).newInstance(ctx.applicationMonitors) }
            .toSet()

        simpleProbes + observerProbes
    }

    private val preActionProbes: Set<Probe<AndroidState>> =
        probes.filter { it.categories.contains(ProbeTimingCategory.PRE_ACTION) }.toSet()

    private val postActionProbes: Set<Probe<AndroidState>> =
        probes.filter { it.categories.contains(ProbeTimingCategory.POST_ACTION) }.toSet()

    override fun before() {
        step = 0
        sessionID = UUID.randomUUID()

        probes.forEach { it.start() }
    }

    override fun after() {
        step = -1
        sessionID = null
        probes.forEach { it.stop() }
    }

    // from https://stackoverflow.com/a/49630449 : verify whether a string is 'typeable' on a standard keyboard
    protected val isTypeableRegex = Regex("""^[a-zA-Z0-9~`!@#${'$'}%^&*()_\-+={\[}\]|:;"'<,>.?/  ]*${'$'}""")

    private val TAG = "o.m.a.EspressoLoop"

    override fun step(action: (() -> Unit)?) {
        // stage one
        Log.d(TAG, "Start of a MINT step")
        var partiallyEvaluatedState = obtainConcreteState()
            // stage two
            .flatMap { state -> sample(state, preActionProbes) }
            // stage three
            .flatMap { state -> abstract(state) }
            // stage four
            .flatMap { state -> correlate(state) }
            // stage five
            .flatMap { state -> rules(state, ctx.rules) }

        // If we provided an action for this step, don't execute the action selection / state application here
        if (action == null) {
            partiallyEvaluatedState = partiallyEvaluatedState
                // stage six
                .flatMap { state -> actionSelection(state) }
                // stage seven
                .flatMap { state -> applyAction(state) }
        } else {
            partiallyEvaluatedState = partiallyEvaluatedState
                .flatMap { applyNonMintAction(action, it) }
        }

        partiallyEvaluatedState
            // stage eight
            .redeemWith { state -> sample(state, postActionProbes) }
            // stage nine
            .flatMap { state -> verdicts(state, ctx.oracles) }
            // stage ten
            .flatMap { state -> persist(state) }
            .tap {
                    state ->
                state.query { node -> node.hasAttribute(AndroidConstants.ERROR_MESSAGE) }
                    .map { IllegalStateException(it.attribute(AndroidConstants.ERROR_MESSAGE)) }
                    .firstOrNull()?.let { throw it }
            }

        // next sequence
        step += 1

        Log.d(TAG, "Finished a MINT step")
    }

    // A non-mint derived action has been provided.
    // Indicate that this is the case in the widget tree so it is clear at a later stage
    private fun applyNonMintAction(action: (() -> Unit), state: AndroidState): Either<AndroidState, AndroidState> {
        // Attach a node that states we applied/selected a step action
        val actionableElement = state.appendChildNode(AndroidConstants.ACTION, AndroidConstants.ACTION_NS) {
            it.setAttribute("selected", "true")
            it.setAttribute("stepAction", "true")
        }

        return performAction(state, actionableElement) {
            action.invoke()
        }
    }

    override fun sample(state: AndroidState, probes: Set<Probe<AndroidState>>): Either<AndroidState, AndroidState> {
        for (p in probes) {
            p.measure(state)
        }
        return Either.Right(state)
    }

    override fun abstract(state: AndroidState): Either<AndroidState, AndroidState> {
        state.extend(ctx.abstract)
        state.extend(ctx.action_abstraction)
        state.createParentHashes()
        return Either.Right(state)
    }

    override fun correlate(state: AndroidState): Either<AndroidState, AndroidState> {
        return Either.Right(ctx.repository.correlate(state))
    }

    override fun rules(state: AndroidState, rules: Set<Rule<AndroidState>>): Either<AndroidState, AndroidState> {
        // create rules group
        state.extendWithRuleGroups()

        // rules to be added to the rule-group
        return Either.Right(
            rules.fold(state) { newState, rule ->
                newState.apply(rule)
                newState
            },
        )
    }

    override fun actionSelection(state: AndroidState): Either<AndroidState, AndroidState> {
        val actionSelection = state.copy()
        return actionSelection.selectAction()
    }

    /**
     * Actually perform an action on a given element, or attach the cause of its failure to it
     */
    private fun performAction(state: AndroidState, actionableElement: Element, effect: (Element) -> Unit): Either<AndroidState, AndroidState> {
        return try {
            effect(actionableElement)
            actionableElement.setAttribute(AndroidConstants.APPLIED, "true")
            Either.Right(state)
        } catch (e: java.lang.Exception) {
            actionableElement.setAttribute(AndroidConstants.APPLIED, "false")

            // any exception encountered when performing the action will be included in the state together with its chain
            // two situations can occur:
            // either the exception originates in the testing framework due to inability to perform the action
            // or the application throws an exception when the action has been performed
            // distinguishing between these two cases relies on the testing framework's implementation details,
            // which is why it is not done here
            val exceptionNode = actionableElement.ownerDocument.createElement("exception")
            ThrowableAttributes.apply(exceptionNode, e)
            actionableElement.appendChild(exceptionNode)

            Either.Left(state)
        }
    }

    override fun applyAction(state: AndroidState): Either<AndroidState, AndroidState> {
        val applyAction = state.copy()
        val selected = applyAction.query {
            it.hasNS(AndroidConstants.ACTION_NS) &&
                "true" == it.attribute("selected") &&
                it.notCorrelated()
        }

        // Nothing is selected, so mark the document as such
        if (selected.isEmpty()) {
            applyAction.appendChildNode(AndroidConstants.ACTION, AndroidConstants.ACTION_NS) {
                it.setAttribute(AndroidConstants.MISSING, "true")
                it.setAttribute(AndroidConstants.COUNT, "0")
                it.setAttribute(
                    AndroidConstants.ERROR_MESSAGE,
                    "Invalid SUT: No actionable items found. " +
                        "Please check if screen has valid widgets that MINT can interact with.",
                )
            }
            return Either.Left(applyAction)
        } else {
            assert(selected.size == 1) {
                // If there somehow are multiple actions, set a count attribute to indicate how many have been found.
                applyAction.appendChildNode(AndroidConstants.ACTION, AndroidConstants.ACTION_NS) {
                    it.setAttribute(AndroidConstants.MISSING, "false")
                    it.setAttribute(AndroidConstants.COUNT, selected.size.toString())
                }
                Log.e(
                    TAG,
                    "Current state unexpectedly contains multiple selected actions:\n" +
                        AndroidStateUtils.renderXML(applyAction.node),
                )
                "Multiple actions were selected in the same step, though only one is expected"
            }

            val s = selected.first() as Element
            // We have to be careful when performing the action, as it can throw an exception
            // for example when Espresso borks on something.
            return performAction(applyAction, s) {
                perform(it)
            }
        }
    }

    override fun verdicts(state: AndroidState, oracles: Set<Oracle<AndroidState>>): Either<AndroidState, AndroidState> {
        for (oracle in oracles) {
            oracle.eval(state)
        }
        return Either.Right(state)
    }

    override fun persist(state: AndroidState): Either<AndroidState, AndroidState> {
        return Either.Right(ctx.repository.persist(state))
    }

    // perform action
    abstract fun perform(n: Element): Unit
}
