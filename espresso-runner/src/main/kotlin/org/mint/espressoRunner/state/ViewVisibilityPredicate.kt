package org.mint.espressoRunner.state

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers

interface ViewVisibilityPredicate {
    fun test(v: View): Boolean
}

class EspressoViewVisibilityPredicate : ViewVisibilityPredicate {

    override fun test(v: View): Boolean {
        return ViewMatchers.isDisplayingAtLeast(
            ESPRESSO_VISIBILITY_THRESHOLD_FOR_ACTIONS
        ).matches(v)
    }

    companion object {
        internal const val ESPRESSO_VISIBILITY_THRESHOLD_FOR_ACTIONS = 90
    }
}
