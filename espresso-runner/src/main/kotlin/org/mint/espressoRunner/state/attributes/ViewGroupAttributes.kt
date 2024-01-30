package com.ing.mint.espressoRunner.state.attributes

import android.view.ViewGroup
import android.webkit.WebView
import android.widget.HorizontalScrollView
import android.widget.ListView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.ViewPager
import org.w3c.dom.Element

internal object ViewGroupAttributes {
    fun apply(node: Element, view: ViewGroup) {
        node.setAttribute("childCount", view.childCount.toString())

        when (view) {
            is ScrollView -> {
                node.setAttribute("isScrollView", "true")
                node.setAttribute("isScrollable", "true")
                node.setAttribute("isSmoothScrollingEnabled", view.isSmoothScrollingEnabled.toString())
            }
            is HorizontalScrollView -> {
                node.setAttribute("isHorizontalScrollView", "true")
                node.setAttribute("isScrollable", "true")
                node.setAttribute("isSmoothScrollingEnabled", view.isSmoothScrollingEnabled.toString())
            }
            is ListView -> {
                node.setAttribute("isListView", "true")
                node.setAttribute("isScrollable", "true")
            }
            is NestedScrollView -> {
                node.setAttribute("isNestedScrollView", "true")
                node.setAttribute("isScrollable", "true")
            }
            is WebView -> node.setAttribute("isWebView", "true")
            is androidx.appcompat.widget.SearchView -> {
                node.setAttribute("isSearchView", "true")
                node.setAttribute("isIconified", view.isIconified.toString())
                node.setAttribute(
                    "isSubmitButtonEnabled",
                    view.isSubmitButtonEnabled.toString(),
                )
            }
            is ViewPager -> {
                node.setAttribute("isViewPager", "true")
                node.setAttribute("currentPage", view.currentItem.toString())
                node.setAttribute("canScrollLeft", (view.currentItem > 0).toString())
                node.setAttribute("canScrollRight", (view.currentItem < view.childCount - 1).toString())
            }
        }
    }
}
