package org.mint.espressoRunner.actions

import androidx.test.platform.app.InstrumentationRegistry

object DeviceRotationChange {
    private const val CMD_USER_ROTATION =
        "settings put system user_rotation %d"

    fun randomRotation() {
        setRotation(Rotation.values().random())
    }

    fun setRotation(rotation: Rotation) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.executeShellCommand(String.format(CMD_USER_ROTATION, rotation.ordinal))
    }

    enum class Rotation {
        PORTRAIT,
        LANDSCAPE
    }
}
