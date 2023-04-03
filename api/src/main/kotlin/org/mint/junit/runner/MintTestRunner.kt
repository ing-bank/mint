package org.mint.junit.runner

import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.parameterized.TestWithParameters

class MintTestRunner(private var t: TestWithParameters) : BlockJUnit4ClassRunner(t.testClass.javaClass) {

    override fun testName(method: FrameworkMethod?): String {
        return method?.name.plus("[".plus(t.parameters.first().toString()).plus("]"))
    }
}
