package org.mint

import org.junit.runners.model.Statement

class RepeatableTestStatement(private val numberOfTimes: Int, private val stmt: Statement) : Statement() {
    override fun evaluate() {
        val throwables = mutableListOf<Throwable>()
        repeat(numberOfTimes) {
            try {
                stmt.evaluate()
            } catch (e: Throwable) {
                throwables.add(e)
            }
        }

        if (throwables.isNotEmpty()) {
            // print all throwables for debugging purposes, and just rethrow the last one for JUnit
            for (t in throwables) {
                t.printStackTrace()
            }
            throw throwables.first()
        }
    }
}
