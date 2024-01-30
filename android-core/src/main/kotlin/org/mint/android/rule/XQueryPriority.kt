package com.ing.mint.android.rule

import com.ing.mint.android.AndroidState
import com.ing.mint.android.AndroidStateUtils
import net.sf.saxon.value.NumericValue
import java.math.BigDecimal

data class XQueryPriority(val xquery: String) : (AndroidState) -> BigDecimal {
    private val eval = AndroidStateUtils.xqueryCompiler().compile(xquery).load()

    override fun invoke(p1: AndroidState): BigDecimal {
        eval?.contextItem = AndroidStateUtils.toXdm(p1.node)
        val i = eval?.evaluateSingle()

        return if (i != null) {
            val k = i.underlyingValue
            if (k is NumericValue) {
                k.decimalValue
            } else {
                throw RuntimeException("XQueryPriorities should return numeric value but another value is given: ${k.stringValue}")
            }
        } else {
            throw RuntimeException("XQueryPriorities should return numeric values but nothing is found.")
        }
    }
}
