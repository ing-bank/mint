package com.ing.mint.espressoRunner

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.Root
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.RootMatchers
import org.hamcrest.Description
import org.hamcrest.Matchers.any
import org.hamcrest.Matchers.anyOf
import org.hamcrest.TypeSafeMatcher

/**
 * Retrieves the roots of the view hierarchies.
 * Multiple view hierarchies exist when pop-up, dialog or system alert windows are present,
 * besides the main application window.
 */
object GetRoots : () -> List<View> {
    override fun invoke(): List<View> {
        val m = RootM()
        // get the root of the current window
        Espresso.onView(m).check(doesNotExist())
        val roots: MutableList<View> = m.roots
        // get the respective roots of any sub-windows
        val subWindowRootMatchers = listOf(
            RootMatchers.isPlatformPopup(),
            RootMatchers.isDialog(),
            RootMatchers.isSystemAlertWindow(),
        )
        subWindowRootMatchers.forEach { subWindowRootMatcher ->
            Espresso.onView(m)
                // the generic matcher is needed as a fallback in case no sub-windows exist
                .inRoot(anyOf(subWindowRootMatcher, any(Root::class.java)))
                .check(NoOpAssertion())
            roots.addAll(m.roots)
        }
        return roots.distinct()
    }

    class RootM : TypeSafeMatcher<View>() {
        var roots: MutableList<View> = mutableListOf()

        override fun matchesSafely(item: View): Boolean {
            if (item.rootView === item) { roots.add(item) }
            return false
        }

        override fun describeTo(description: Description) {
            description.appendText("ROOT HIERARCHIES SNAP")
        }
    }

    internal class NoOpAssertion : ViewAssertion {
        override fun check(view: View?, noView: NoMatchingViewException?) {
            // do nothing
        }
    }
}
