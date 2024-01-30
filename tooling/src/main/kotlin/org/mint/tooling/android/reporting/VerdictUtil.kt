package com.ing.mint.tooling.android.reporting

import com.ing.mint.android.xml.attribute
import org.w3c.dom.Element
import org.w3c.dom.Node

object VerdictUtil {
    /** Given a set of steps, count all verdicts that have been applied throughout all of the steps */
    fun Set<Node>.countVerdicts(): Pair<Int, Int> {
        var totalVerdicts = 0
        var totalNotOkVerdicts = 0
        for (s in this) {
            val verdicts = (s as Element).getElementsByTagName("verdict")
            totalVerdicts += verdicts.length
            for (n in 0 until verdicts.length) {
                if (verdicts.item(n).attribute("decision") != "OK") {
                    totalNotOkVerdicts += 1
                }
            }
        }
        return Pair(totalVerdicts, totalNotOkVerdicts)
    }
}
