package com.ing.mint.android.rule.input

import com.ing.mint.android.AndroidConstants
import com.ing.mint.android.AndroidState
import com.ing.mint.android.rule.BaseRule
import org.w3c.dom.Element

abstract class BaseInputRule : BaseRule() {
    abstract fun generate(): (AndroidState) -> String

    override fun attributes(state: AndroidState, action: Element) {
        action.setAttribute(AndroidConstants.TEXT, generate()(state))
    }
}
