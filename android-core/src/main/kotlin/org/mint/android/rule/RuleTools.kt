package org.mint.android.rule

import org.mint.android.AndroidState
import org.w3c.dom.Element

interface RuleTools {
    /** Apply any optional, additional attributes to a newly created action if needed */
    fun attributes(state: AndroidState, action: Element) {}

    /** Derive the rule name of this Rule */
    fun ruleName(): String {
        var ret = javaClass.canonicalName
        if (ret != null) {
            return ret
        }
        // If an anonymous class is involved, canonicalName is null and simpleName ""
        ret = javaClass.name
        if (ret != null) {
            return ret.replace('$', '-')
        }
        throw IllegalStateException("No rule name can be derived")
    }
}
