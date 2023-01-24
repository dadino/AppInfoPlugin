package com.appinfoplugin

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.ReadOnlyProductFlavor
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import org.gradle.internal.extensibility.DefaultExtraPropertiesExtension


class AppInfoBuilder {

    fun fromVariant(
        variant: BaseVariant,
        defaultVersionName: String,
        defaultVersionCode: Int
    ): AppInfo {
        val manifest = getManifestFile(variant)
        val packageName = getPackageName(manifest)
        val versionCode = defaultVersionCode
        val versionName = defaultVersionName

        val environmentFlavor = variant.productFlavors.firstOrNull { flavor ->
            val environment = if (flavor is ReadOnlyProductFlavor) {
                val extras =
                    (flavor.getProperty("ext") as DefaultExtraPropertiesExtension).properties
                extras["environment"] as? String
            } else null
            environment != null
        }
        val environment =
            if (environmentFlavor != null && environmentFlavor is ReadOnlyProductFlavor) {
                val extras =
                    (environmentFlavor.getProperty("ext") as DefaultExtraPropertiesExtension).properties
                extras["environment"] as? String
            } else null


        return AppInfo(
            packageName = packageName,
            versionCode = versionCode,
            versionName = versionName,
            environment = environment ?: ""
        )
    }


    private fun getPackageName(manifest: GPathResult): String {
        return manifest.getProperty("@package").toString()
    }

    private fun getManifestFile(variant: BaseVariant): GPathResult {
        val slurper = XmlSlurper(false, false)
        val list = variant.sourceSets.map { it.manifestFile }
        return slurper.parse(list[0])
    }
}