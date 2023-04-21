package org.mint.android

enum class Action(val tagName: String) {
    CLICK("click"),
    CLICK_ON_ADAPTER_VIEW_ITEM("clickOnAdapterViewItem"),
    CLICK_ON_ITEM_AT_POSITION("clickOnItemAtPosition"),
    CLICK_ON_ITEM_AT_POSITION_IN_POPUP("clickOnItemAtPositionInPopup"),
    CLICK_ON_ITEM_WITH_TAG("clickOnItemWithTag"),
    CLICK_ON_SPINNER_ITEM("clickOnSpinnerItem"),
    DATE_PICKER_INPUT("datePickerInput"),
    INPUT("input"),
    SCROLL_PAGER_TO_LEFT("scrollToLeft"),
    SCROLL_PAGER_TO_RIGHT("scrollToRight"),
    SCROLL_TO_AND_CLICK_ITEM_AT_POSITION("scrollToAndClick"),
    TIME_PICKER_INPUT("timePickerInput"),
    DEVICE_ROTATION_CHANGE("deviceRotationChange"),
    DEVICE_THEME_CHANGE("deviceThemeChange"),
}
