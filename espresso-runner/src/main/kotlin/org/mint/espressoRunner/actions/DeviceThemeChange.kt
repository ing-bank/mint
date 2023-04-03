package org.mint.espressoRunner.actions

import androidx.test.platform.app.InstrumentationRegistry
import org.mint.espressoRunner.actions.DeviceThemeChange.Theme.DARK
import org.mint.espressoRunner.actions.DeviceThemeChange.Theme.LIGHT

object DeviceThemeChange {
    private const val CMD_UI_MODE_NIGHT_YES =
        "cmd uimode night yes"

    private const val CMD_UI_MODE_NIGHT_NO =
        "cmd uimode night no"

    fun randomTheme() {
        setTheme(Theme.values().random())
    }

    fun setTheme(theme: Theme) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        when (theme) {
            DARK -> {
                automation.executeShellCommand(CMD_UI_MODE_NIGHT_YES)
            }
            LIGHT -> {
                automation.executeShellCommand(CMD_UI_MODE_NIGHT_NO)
            }
        }
    }

    enum class Theme {
        DARK,
        LIGHT
    }
}
