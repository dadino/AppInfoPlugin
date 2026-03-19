package com.appinfoplugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Locale

class AppInfoPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val androidComponents =
            project.extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
        val applicationExtension = project.extensions.findByType(ApplicationExtension::class.java)

        if (androidComponents == null || applicationExtension == null) {
            project.logger.warn("AppInfoPlugin requires the Android Application plugin to be applied.")
            return
        }

        // Use the new Variant API
        androidComponents.onVariants(
            androidComponents.selector().withBuildType("release")
        ) { variant ->
            val variantName =
                variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            // Extract environment from flavor extensions
            val environmentValue = variant.productFlavors.mapNotNull { (_, flavorName) ->
                val flavor = applicationExtension.productFlavors.findByName(flavorName)
                val ext = flavor?.extensions?.extraProperties ?: return@mapNotNull null

                when {
                    ext.has("environment") -> ext.get("environment")?.toString()
                    ext.has("move_store_environment") -> ext.get("move_store_environment")
                        ?.toString()

                    else -> null
                }
            }.firstOrNull() ?: ""

            // 1. Register AppInfo Task
            val addAppInfoTask =
                project.tasks.register("addAppInfo$variantName", AddAppInfoTask::class.java) {
                    group = "AppInfo"

                    // Map inputs lazily
                    packageName.set(variant.applicationId)
                    versionName.set(variant.outputs.first().versionName)
                    versionCode.set(variant.outputs.first().versionCode)
                    environment.set(environmentValue)
                }

            // 2. Register AppIcon Task
            val addAppIconTask =
                project.tasks.register("addAppIcon$variantName", AddAppIconTask::class.java) {
                    group = "AppInfo"
                    val relevantSourceSetNames =
                        variant.productFlavors.map { it.second } + variant.buildType + "main"

                    relevantSourceSetNames.forEach { name ->
                        val sourceSet = applicationExtension.sourceSets.findByName(name.toString())
                        if (sourceSet != null) {
                            searchDirs.from(sourceSet.assets.directories)
                            searchDirs.from(sourceSet.res.directories)
                            searchDirs.from(sourceSet.java.directories)
                            searchDirs.from(sourceSet.kotlin.directories)
                        }
                    }
                }

            // 3. Wire tasks directly to the Variant's Asset generation
            variant.sources.assets?.addGeneratedSourceDirectory(
                addAppInfoTask,
                AddAppInfoTask::outputDir
            )
            variant.sources.assets?.addGeneratedSourceDirectory(
                addAppIconTask,
                AddAppIconTask::outputDir
            )
        }
    }

    companion object {
        internal const val TAG = "AppInfoPlugin"
    }
}

