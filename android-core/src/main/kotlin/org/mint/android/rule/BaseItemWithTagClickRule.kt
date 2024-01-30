package com.ing.mint.android.rule

import com.ing.mint.android.AndroidConstants
import com.ing.mint.android.AndroidState
import org.w3c.dom.Element

abstract class BaseItemWithTagClickRule : BaseRule() {
    abstract fun itemTag(): (AndroidState) -> String

    override fun attributes(state: AndroidState, action: Element) {
        action.setAttribute(AndroidConstants.TAG, itemTag()(state))
    }
}
