package org.mint.espressoRunner.matchers

import android.view.View
import android.view.ViewGroup
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(JUnitParamsRunner::class)
internal class ViewAtPositionTest {
    private val root: ViewGroup = mock()
    private val child: ViewGroup = mock()
    private val grandchild1: View = mock()
    private val grandchild2: View = mock()

    @Before
    fun setup() {
        listOf(root, child, grandchild1, grandchild2).forEach {
            whenever(it.rootView).thenReturn(root)
        }
        whenever(root.getChildAt(0)).thenReturn(child)
        whenever(child.getChildAt(0)).thenReturn(grandchild1)
        whenever(child.getChildAt(1)).thenReturn(grandchild2)
    }

    @Test
    fun validPosition() {
        assert(ViewAtPosition("root.0").matches(child))
        assert(ViewAtPosition("root.0.0").matches(grandchild1))
        assert(ViewAtPosition("root.0.1").matches(grandchild2))
    }

    @Test
    @Parameters(
        value = [
            "root.0.1.1",
            "",
            "root.0.2",
        ],
    )
    fun noMatches(position: String) {
        val viewAtPositionMatcher = ViewAtPosition(position)

        assert(not(viewAtPositionMatcher).matches(grandchild2))
        assert(not(viewAtPositionMatcher).matches(root))
        assert(not(viewAtPositionMatcher).matches(child))
        assert(not(viewAtPositionMatcher).matches(grandchild1))
    }
}
