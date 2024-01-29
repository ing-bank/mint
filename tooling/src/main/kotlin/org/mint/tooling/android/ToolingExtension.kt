package com.ing.mint.tooling.android

import org.gradle.api.tasks.Optional

abstract class ToolingExtension {
    @set: Optional
    abstract var targetDir: String?
    abstract var packageName: String?
}
