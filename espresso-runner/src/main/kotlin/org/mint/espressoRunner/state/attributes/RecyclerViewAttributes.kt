package com.ing.mint.espressoRunner.state.attributes

import androidx.recyclerview.widget.RecyclerView
import com.ing.mint.espressoRunner.state.ViewVisibilityPredicate
import org.w3c.dom.Element

data class RecyclerViewAttributes(val viewVisibilityPredicate: ViewVisibilityPredicate) {

    fun apply(node: Element, view: RecyclerView) {
        node.setAttribute("isRecyclerView", "true")

        val adapter = view.adapter
        node.setAttribute("itemCount", adapter?.itemCount.toString())
    }
}
