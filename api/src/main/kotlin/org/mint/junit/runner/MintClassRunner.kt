package org.mint.junit.runner

import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Runner
import org.junit.runners.Suite
import org.junit.runners.model.TestClass
import org.junit.runners.parameterized.TestWithParameters
import org.mint.MINTRule

@Suppress("unused")
class MintClassRunner(testClass: Class<*>?) : Suite(testClass, runners(testClass)) {

    companion object {

        fun runners(t: Class<*>?): List<Runner> {
            val testClass = TestClass(t)

            val target = testClass.onlyConstructor.newInstance()
            val l = testClass.getAnnotatedFieldValues(
                target,
                Rule::class.java,
                TestRule::class.java
            ).filterIsInstance<MINTRule>().first().numberOfSequences()

            val testRunners: MutableList<MintTestRunner> = mutableListOf()

            for (i in 0 until l) {
                testRunners.add(
                    MintTestRunner(
                        TestWithParameters(
                            "[${testClass.name}]",
                            testClass,
                            listOf("$i")
                        )
                    )
                )
            }

            return testRunners
        }
    }
}
