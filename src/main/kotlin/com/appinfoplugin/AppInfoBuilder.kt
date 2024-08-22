package com.appinfoplugin

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.ReadOnlyProductFlavor
import org.gradle.internal.extensibility.DefaultExtraPropertiesExtension


class AppInfoBuilder {

    fun fromVariant(
        variant: BaseVariant,
        defaultVersionName: String,
        defaultVersionCode: Int
    ): AppInfo {
        //val manifest = getManifestFile(variant)
        val packageName = getPackageName(variant)
        val versionCode = defaultVersionCode
        val versionName = defaultVersionName

        val environmentFlavor = variant.productFlavors.firstOrNull { flavor ->
            val environment = if (flavor is ReadOnlyProductFlavor) {
                val extras =
                    (flavor.getProperty("ext") as DefaultExtraPropertiesExtension).properties
                getEnvironment(extras)
            } else null
            environment != null
        }
        val environment =
            if (environmentFlavor != null && environmentFlavor is ReadOnlyProductFlavor) {
                val extras =
                    (environmentFlavor.getProperty("ext") as DefaultExtraPropertiesExtension).properties
                getEnvironment(extras)
            } else null


        return AppInfo(
            packageName = packageName,
            versionCode = versionCode,
            versionName = versionName,
            environment = environment ?: ""
        )
    }

    private fun getEnvironment(extras: Map<String, Any>): String? =
        (extras["environment"] as? String) ?: (extras["move_store_environment"] as? String)

    private fun getPackageName(variant: BaseVariant): String {
        val suffix = variant.buildType.applicationIdSuffix
        val packageName = variant.productFlavors[0].applicationId
        if (suffix.isNullOrBlank().not()) {
            return "$packageName$suffix"
        }
        return packageName ?: ""
    }
}