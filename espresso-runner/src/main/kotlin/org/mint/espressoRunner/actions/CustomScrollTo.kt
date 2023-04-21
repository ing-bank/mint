package org.mint.espressoRunner.actions

import android.graphics.Rect
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ListView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import org.hamcrest.Matcher
import org.hamcrest.Matchers.anyOf

/**
 * Scrolls to the given view, if it is a descendant of scrollable views.
 * Similar to [androidx.test.espresso.action.ScrollToAction], but unlike it,
 * it also accepts descendants of NestedScrollView and skips the verification of visibility on screen,
 * since that's already checked.
 */
class CustomScrollTo : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isDescendantOfA(
            anyOf(
                isAssignableFrom(ScrollView::class.java),
                isAssignableFrom(HorizontalScrollView::class.java),
                isAssignableFrom(ListView::class.java),
                isAssignableFrom(NestedScrollView::class.java),
            ),
        )
    }

    override fun getDescription(): String {
        return "Scroll to views that are descendants of scrollable views"
    }

    override fun perform(uiController: UiController?, view: View?) {
        val rect = Rect()
        view!!.getDrawingRect(rect)
        if (!view.requestRectangleOnScreen(rect, true)) {
            println("Scrolling to view was requested, but none of the parents scrolled.")
        }
        uiController!!.loopMainThreadUntilIdle()
    }
}
