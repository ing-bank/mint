package org.mint

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.mint.android.TestMetadata

class TestMetadataRule : TestRule {
    private var metadata: TestMetadata? = null

    override fun apply(base: Statement, description: Description): Statement {
        metadata = TestMetadata(description.className, description.methodName, description.displayName)
        return base
    }

    fun metadata(): TestMetadata? {
        return metadata
    }
}
