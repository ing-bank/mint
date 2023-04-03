package org.mint.espressoRunner.state

import android.content.pm.PackageManager
import androidx.test.platform.app.InstrumentationRegistry
import org.w3c.dom.Document
import org.w3c.dom.Node

internal object AppInfo {
    private var appInfo: AppInfo? = null

    fun toNode(doc: Document): Node? {
        appInfo ?: run {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val applicationContext = instrumentation.targetContext
            val packageManager = applicationContext.packageManager
            val applicationPackage = applicationContext.packageName

            try {
                val packageInfo = packageManager.getPackageInfo(
                    applicationPackage,
                    PackageManager.GET_PERMISSIONS
                )

                val applicationName =
                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
                val applicationVersion = packageInfo.versionName
                val requestedPermissions = packageInfo.requestedPermissions

                appInfo =
                    AppInfo(
                        applicationPackage,
                        applicationName,
                        applicationVersion,
                        requestedPermissions
                    )
            } catch (e: PackageManager.NameNotFoundException) {
                println("Could not retrieve the app info, $e")
                return null
            }
        }

        val appInfoNode = doc.createElement("AppInfo")
        appInfo!!.applicationPackageName?.let {
            appInfoNode.setAttribute(
                "applicationPackageName",
                it
            )
        }
        appInfo!!.applicationName?.let { appInfoNode.setAttribute("applicationName", it) }
        appInfo!!.applicationVersion?.let { appInfoNode.setAttribute("applicationVersion", it) }
        appInfo!!.requestedPermissions?.let {
            val permissions = doc.createElement("Permissions")

            for (i in it.indices) {
                val permission = doc.createElement("Permission")
                permission.setAttribute("name", it[i])
                permissions.appendChild(permission)
            }
            appInfoNode.appendChild(permissions)
        }
        return appInfoNode
    }

    data class AppInfo(
        val applicationPackageName: String?,
        val applicationName: String?,
        val applicationVersion: String?,
        val requestedPermissions: Array<String>?
    )
}
