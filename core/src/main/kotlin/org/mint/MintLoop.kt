package org.mint

import org.mint.lib.Oracle
import org.mint.lib.Probe
import org.mint.lib.Rule
import org.mint.lib.RunContext
import org.mint.lib.SUTState
import org.mint.util.Either

/** All stages of the mint loop
 *
 * Each stage returns an Either, where both branches are represented by a State. This is because:
 * - A Right is the intended behaviour, where we have an updated State reflecting whatever state
 * just has been applied.
 * - A Left is a failure. However, because of our data-driven approach, a failure is also encoded as
 * part of the system state. Therefore, we can persist it no matter what happened and show this to
 * end user of the tool. The error is encoded in the SUT as closely as we can to the root cause of
 * why the failure happened. This also ensures any related screenshot is also there. */
interface MintLoop<State : SUTState<State>> {
    val ctx: RunContext<State>

    /** Zero: this method is guaranteed to be invoked before anything happens with this mint loop */
    fun before()

    /** One: obtain concrete state from SUT */
    fun obtainConcreteState(): Either<State, State>

    /** Two: sample an application by using a number of probes. Add probe name + version to annotation. Note that the
     * probes will be obtained from the oracles */
    fun sample(state: State, probes: Set<Probe<State>>): Either<State, State>

    /** Three: Extend the ApplicationState with abstract state information (embedded as to retain context) */
    fun abstract(state: State): Either<State, State>

    /** Four: reach out to the repository to obtain correlated application state, embedded into the current application state */
    fun correlate(state: State): Either<State, State>

    /** Five: apply rules to extend the application state with possible actions with their associated relative weights
     * Note that you can early-exit if you create a rule matching a specific scenario (e.g. crash, sink)
     * and create the action 'exit' with high priority
     * Note: the rules themselves should also become annotations in the SUTState */
    fun rules(state: State, rules: Set<Rule<State>>): Either<State, State>

    /** Six: Action selection */
    fun actionSelection(state: State): Either<State, State>

    /** Seven: Apply action */
    fun applyAction(state: State): Either<State, State>
    // TODO: Where should oracles live in the lifecycle? We might want to evalute them before
    // applying an action? Or before the rules, so rules can take the judgements into account.
    /** Eight: Verdicts. Note that the oracles need to be included as annotations as well.  */
    fun verdicts(state: State, oracles: Set<Oracle<State>>): Either<State, State>

    /** Nine: push to repo */
    fun persist(state: State): Either<State, State>

    /** End: this method is guaranteed to be invoked after completing this MINT loop */
    fun after()

    /** Take a single step (i.e. execute a single MINT loop consisting of all individual stages)
     *
     * An optional action can be provided, in which case that given action will be executed instead
     * of the built-in action selection + execution mechanism */
    fun step(action: (() -> Unit)?)
}
