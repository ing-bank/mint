package com.ing.mint

import com.ing.mint.android.TestMetadata
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

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
