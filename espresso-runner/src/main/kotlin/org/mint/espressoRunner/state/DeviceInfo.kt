package org.mint.espressoRunner.state

import org.w3c.dom.Document
import org.w3c.dom.Node

internal object DeviceInfo {
    fun toNode(doc: Document): Node {
        val deviceInfo = doc.createElement("DeviceInfo")

        deviceInfo.setAttribute("buildVersion", android.os.Build.VERSION.SDK_INT.toString())

        // Attributes for emulator detection: https://ray-chong.medium.com/android-emulator-detection-4d0f994aab5e
        deviceInfo.setAttribute("industrialDesignName", android.os.Build.DEVICE)
        deviceInfo.setAttribute("model", android.os.Build.MODEL)
        deviceInfo.setAttribute("brand", android.os.Build.BRAND)
        deviceInfo.setAttribute("product", android.os.Build.PRODUCT)
        deviceInfo.setAttribute("hardware", android.os.Build.HARDWARE)
        deviceInfo.setAttribute("manufacturer", android.os.Build.MANUFACTURER)

        return deviceInfo
    }
}
