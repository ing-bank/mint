package org.mint.espressoRunner.state

import androidx.appcompat.app.AppCompatActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.w3c.dom.Document
import org.w3c.dom.Node

object AppComponents {

    fun toNode(doc: Document): Node? {
        val node = doc.createElement("AppComponents")

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val resumedActivities = ActivityLifecycleMonitorRegistry
                .getInstance()
                .getActivitiesInStage(Stage.RESUMED)

            // when in multi-window mode, there can be multiple activities in state resumed at the same time
            resumedActivities?.let { l ->
                l.forEach { a ->
                    val activityNode = doc.createElement("Activity")
                    activityNode.setAttribute("name", a.localClassName)
                    node.appendChild(activityNode)
                    if (a is AppCompatActivity) {
                        a.supportFragmentManager.fragments.forEach { fg ->
                            if (fg != null) {
                                val fragmentNode = doc.createElement("Fragment")
                                fragmentNode.setAttribute("name", fg.javaClass.toString())
                                fragmentNode.setAttribute("state", fg.lifecycle.currentState.name)
                                node.appendChild(fragmentNode)
                            }
                        }
                    }
                }
            }
        }

        return node
    }
}
