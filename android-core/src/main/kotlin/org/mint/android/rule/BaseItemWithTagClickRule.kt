package org.mint.android.rule

import org.mint.android.AndroidConstants
import org.mint.android.AndroidState
import org.w3c.dom.Element

abstract class BaseItemWithTagClickRule : BaseRule() {
    abstract fun itemTag(): (AndroidState) -> String

    override fun attributes(state: AndroidState, action: Element) {
        action.setAttribute(AndroidConstants.TAG, itemTag()(state))
    }
}
