package com.ing.mint.espressoRunner.matchers

import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ViewAtPosition(private val position: String) : TypeSafeMatcher<View>() {
    private var targetView: View? = null

    override fun matchesSafely(item: View): Boolean {
        val positions: List<String> = position.split('.')

        // espresso will evaluate all views in the hierarchy
        targetView ?: run {
            if (positions.elementAt(0) == "root") {
                var view = item.rootView

                for (i in 1 until positions.size - 1) {
                    if (view is ViewGroup) {
                        view = view.getChildAt(positions.elementAt(i).toInt())
                    } else {
                        return false
                    }
                }
                if (positions.size > 1 && view is ViewGroup) {
                    targetView = view.getChildAt(positions.last().toInt())
                }
            }
        }
        return targetView == item
    }

    override fun describeTo(description: Description) {
        description.appendText("View at view hierarchy position $position")
    }
}
