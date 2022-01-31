package com.appinfoplugin

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.ReadOnlyProductFlavor
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import org.gradle.internal.extensibility.DefaultExtraPropertiesExtension


class AppInfoBuilder {

    fun fromVariant(variant: BaseVariant, defaultVersionName: String, defaultVersionCode: Int): AppInfo {
        val manifest = getManifestFile(variant)
        val packageName = getPackageName(manifest)
        var versionCode = defaultVersionCode
        var versionName = defaultVersionName

        val flavors = variant.productFlavors.mapNotNull { flavor ->
            val flavorVersionCode = flavor.versionCode
            val flavorVersionName = flavor.versionName
            versionName =
                flavorVersionName?.let { if (versionCode < flavorVersionCode ?: 0) flavorVersionName else versionName }
                    ?: versionName
            versionCode =
                flavorVersionCode?.let { if (versionCode < flavorVersionCode) flavorVersionCode else versionCode }
                    ?: versionCode

            if (flavor is ReadOnlyProductFlavor) {

                val extras = (flavor.getProperty("ext") as DefaultExtraPropertiesExtension).properties
                val flavorName = extras["flavorName"] as? String
                val dimensionName = extras["dimensionName"] as? String
                val dimensionId = flavor.dimension

                if (flavorName != null && dimensionName != null && dimensionId != null) {
                    FlavorInfo(
                        flavorId = flavor.name,
                        flavorName = flavorName,
                        dimensionId = dimensionId,
                        dimensionName = dimensionName
                    )
                } else null
            } else null
        }

        return AppInfo(
            packageName = packageName,
            versionKey = createVersionKey(flavors),
            versionCode = versionCode,
            versionName = versionName,
            flavors = flavors
        )
    }

    private fun createVersionKey(flavors: List<FlavorInfo>): String {
        return flavors.joinToString("-") { it.flavorId }
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