package com.ing.mint.espressoRunner.state
import com.ing.mint.android.AndroidLoop
import org.w3c.dom.Node
import java.text.SimpleDateFormat
import java.util.*

class LoopMetaDetails(val loop: AndroidLoop) : (Node) -> Node? {

    companion object {
        private val dateF = SimpleDateFormat("yyyy-MM-dd")
        private val timeF = SimpleDateFormat("HH:mm:ss")
        private val milliF = SimpleDateFormat("SSS")
        private val zoneF = SimpleDateFormat("a")
    }

    override fun invoke(node: Node): Node? {
        return if ("SystemUnderTest" == node.nodeName) {
            val doc = node.ownerDocument
            val elem = doc.createElement("AndroidLoop")

            elem.setAttribute("session", loop.sessionID.toString())
            elem.setAttribute("sequence", loop.sequence)
            elem.setAttribute("step", loop.step.toString())

            val now = Calendar.getInstance().getTime()

            elem.setAttribute("date", dateF.format(now))
            elem.setAttribute("time", timeF.format(now))
            elem.setAttribute("millis", milliF.format(now))
            elem.setAttribute("zone", zoneF.format(now))

            elem
        } else {
            null
        }
    }
}
