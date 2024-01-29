package com.ing.mint.android.oracle.accessibility

import com.ing.mint.android.AndroidConstants
import com.ing.mint.android.AndroidState
import com.ing.mint.android.xml.appendChild
import com.ing.mint.android.xml.attribute
import com.ing.mint.android.xml.hasAttribute
import com.ing.mint.android.xml.parent
import com.ing.mint.lib.Oracle
import com.ing.mint.lib.Probe
import com.ing.mint.lib.Verdict
import kotlinx.coroutines.runBlocking
import org.w3c.dom.Node

/**
 * Base Oracle class for checks from the Accessibility Test Framework
 */
abstract class AccessibilityOracle : Oracle<AndroidState> {

    companion object {
        private const val NODE_VIEW = "View"
        private const val NODE_VIEW_CHECK = "view-check"

        private const val ATTR_TYPE = "type"
        private const val ATTR_RESULT = "result"
        private const val ATTR_RESULT_ERROR = "ERROR"
        private const val ATTR_RESULT_WARNING = "WARNING"
        private const val ATTR_RESULT_INFO = "INFO"
        private const val ATTR_RESULT_RESOLVED = "RESOLVED"
        private const val ATTR_MESSAGE = "message"
        private const val ATTR_DECISION = "decision"

        private const val ELEMENT_VERDICT = "verdict"
    }

    abstract val checkType: String

    override fun probes(): Set<Class<out Probe<AndroidState>>> = emptySet()

    override fun eval(state: AndroidState): AndroidState = runBlocking {
        val viewChecks = state.queryViewChecks()
        if (viewChecks.isEmpty()) {
            state.node.addVerdict(
                Verdict.OK,
            )
        }
        viewChecks.forEach { viewCheck ->
            val viewNode = viewCheck.viewNodeFromViewCheck()
            if (viewCheck.isError()) {
                viewNode?.addVerdict(
                    Verdict.FAIL,
                    message = viewCheck.attribute(ATTR_MESSAGE),
                )
            } else if (viewCheck.isWarning()) {
                viewNode?.addVerdict(
                    Verdict.WARNING,
                    message = viewCheck.attribute(ATTR_MESSAGE),
                )
            } else if (viewCheck.isInfo() || viewCheck.isResolved()) {
                viewNode?.addVerdict(
                    Verdict.INFO,
                    message = viewCheck.attribute(ATTR_MESSAGE),
                )
            }
        }
        state
    }

    private fun AndroidState.queryViewChecks() = query { node ->
        node.run {
            isViewCheck() &&
                isOfType()
        }
    }

    private fun Node.isView(): Boolean {
        return nodeName == NODE_VIEW
    }

    private fun Node.isViewCheck(): Boolean {
        return nodeName == NODE_VIEW_CHECK
    }

    private fun Node.isOfType(): Boolean {
        return hasAttribute(ATTR_TYPE) &&
            attribute(ATTR_TYPE)!! == checkType
    }

    private fun Node.isError(): Boolean {
        return hasAttribute(ATTR_RESULT) &&
            attribute(ATTR_RESULT)!! == ATTR_RESULT_ERROR
    }

    private fun Node.isWarning(): Boolean {
        return hasAttribute(ATTR_RESULT) &&
            attribute(ATTR_RESULT)!! == ATTR_RESULT_WARNING
    }

    private fun Node.isInfo(): Boolean {
        return hasAttribute(ATTR_RESULT) &&
            attribute(ATTR_RESULT)!! == ATTR_RESULT_INFO
    }

    private fun Node.isResolved(): Boolean {
        return hasAttribute(ATTR_RESULT) &&
            attribute(ATTR_RESULT)!! == ATTR_RESULT_RESOLVED
    }

    /**
     * Convenience function to get the View node from the view-check node.
     *
     * Structure:
     * <View> --> This is the target node
     *     <accessibility-checks>
     *         <view-check> --> Currently here
     *             <metadata/>
     *         </view-check>
     *     </accessibility-checks>
     * <View>
     */
    private fun Node.viewNodeFromViewCheck(): Node? {
        return if (parent().parent().isView()) {
            parent().parent()
        } else {
            null
        }
    }

    /**
     * This adds a verdict to the View Node with a message.
     */
    private fun Node.addVerdict(
        verdict: Verdict,
        message: String? = null,
    ) {
        val verdictAttributes = mutableListOf(
            Pair(ATTR_DECISION, verdict.name),
        )

        message?.let { value ->
            verdictAttributes.add(
                Pair(ATTR_MESSAGE, value),
            )
        }

        appendChild(
            tagName = name,
            namespace = AndroidConstants.ORACLE_NS,
        ).appendChild(
            tagName = ELEMENT_VERDICT,
            attributes = verdictAttributes,
        )
    }
}
