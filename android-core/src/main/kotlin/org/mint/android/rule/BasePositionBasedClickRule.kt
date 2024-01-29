package com.ing.mint.android.rule

import com.ing.mint.android.AndroidConstants
import com.ing.mint.android.AndroidState
import org.w3c.dom.Element

abstract class BasePositionBasedClickRule : BaseRule() {
    abstract fun itemPosition(): (AndroidState) -> String

    override fun attributes(state: AndroidState, action: Element) {
        action.setAttribute(AndroidConstants.POSITION, itemPosition()(state))
    }
}
