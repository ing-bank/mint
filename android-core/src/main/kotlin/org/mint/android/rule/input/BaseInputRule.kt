package org.mint.android.rule.input

import org.mint.android.AndroidConstants
import org.mint.android.AndroidState
import org.mint.android.rule.BaseRule
import org.w3c.dom.Element

abstract class BaseInputRule : BaseRule() {
    abstract fun generate(): (AndroidState) -> String

    override fun attributes(state: AndroidState, action: Element) {
        action.setAttribute(AndroidConstants.TEXT, generate()(state))
    }
}
