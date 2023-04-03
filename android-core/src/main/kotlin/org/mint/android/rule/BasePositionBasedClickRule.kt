package org.mint.android.rule

import org.mint.android.AndroidConstants
import org.mint.android.AndroidState
import org.w3c.dom.Element

abstract class BasePositionBasedClickRule : BaseRule() {
    abstract fun itemPosition(): (AndroidState) -> String

    override fun attributes(state: AndroidState, action: Element) {
        action.setAttribute(AndroidConstants.POSITION, itemPosition()(state))
    }
}
