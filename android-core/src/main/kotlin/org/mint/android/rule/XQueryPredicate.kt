package com.ing.mint.android.rule

import com.ing.mint.android.AndroidState
import com.ing.mint.android.AndroidStateUtils
import net.sf.saxon.value.BooleanValue

data class XQueryPredicate(val xquery: String) : (AndroidState) -> Boolean {
    private val eval = AndroidStateUtils.xqueryCompiler().compile(xquery).load()

    /**
     * Evaluate an XQuery on an espresso state as a predicate.
     *
     * If the predicate matches a value, we evaluate the value itself:
     * - it is a boolean value --> the predicate holds iff the value is true
     * - it is any other value --> the predicate holds
     *
     * We consider the runtime value for the XQuery evaluation result because a predicate
     * implies boolean logic and we can then use it to make decisions based on the actual state of
     * the SUT DOM
     */
    override fun invoke(s: AndroidState): Boolean {
        eval?.contextItem = AndroidStateUtils.toXdm(s.node)
        val i = eval?.evaluateSingle()

        return if (i != null) {
            val k = i.underlyingValue
            if (k is BooleanValue) k.isIdentical(BooleanValue.TRUE) else true
        } else {
            false
        }
    }
}
