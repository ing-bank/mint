package org.mint.android.probe

import org.mint.android.AndroidConstants
import org.mint.android.AndroidState
import org.mint.lib.Probe
import org.w3c.dom.Node

interface AndroidProbe : Probe<AndroidState> {
    /** Retrieve or create the top level node for a given probe */
    fun probeNode(state: AndroidState, query: (Node) -> Boolean): AndroidState {
        val probeResult = state.query(query)

        val probeNS: Node = if (probeResult.isEmpty()) {
            state.appendChildNode(name, AndroidConstants.PROBE_NS)
        } else {
            probeResult.first()
        }

        return state.derive(probeNS)
    }
}
