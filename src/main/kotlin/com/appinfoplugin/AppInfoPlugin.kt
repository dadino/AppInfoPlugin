package com.appinfoplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import com.appinfoplugin.AddAppIconTask.Companion.APP_ICON_NAME
import kotlinx.serialization.json.Json
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.closureOf
import java.io.File
import java.util.*
import kotlin.reflect.KClass

class AppInfoPlugin : Plugin<Project> {
    private val jsonEncoder = Json {
        prettyPrint = true
    }

    override fun apply(project: Project) {
        project.extensions[AppExtension::class].run {
            configureTasks(project, this, applicationVariants)
        }
    }

    private fun configureTasks(
        project: Project,
        appExtension: AppExtension,
        variants: DomainObjectSet<out BaseVariant>
    ) {
        variants.all {
            if (buildType.name == "release") {
                val mergeAssetsTask = this.mergeAssetsProvider.get()
                val compressAssetsTask =
                    project.tasks.getByName("compress${this.name.capitalize(Locale.ROOT)}Assets")
                val processResourcesTask =
                    project.tasks.getByName("process${this.name.capitalize(Locale.ROOT)}Resources")


                val appAppIconTaskName = "addAppIcon${name.capitalize(Locale.ROOT)}"
                val addAppIconTask =
                    project.tasks.create(appAppIconTaskName, AddAppIconTask::class.java) {
                        group = "AppInfo"
                        outputDir.set(this@all.mergeAssetsProvider.get().outputDir)
                        val appIconFile = searchForAppIcon(this@all)
                        if (appIconFile != null) appIcon.set(
                            project.objects.fileProperty().fileValue(appIconFile)
                        )

                        println("${this.name}: Configuring previous task ${compressAssetsTask.name}")
                        dependsOn(mergeAssetsTask)
                        mustRunAfter(mergeAssetsTask)
                    }

                val appAppInfoTaskName = "addAppInfo${name.capitalize(Locale.ROOT)}"
                val addAppInfoTask =
                    project.tasks.create(appAppInfoTaskName, AddAppInfoTask::class.java) {
                        group = "AppInfo"
                        outputDir.set(this@all.mergeAssetsProvider.get().outputDir)
                        buildNumber = System.currentTimeMillis().toString()
                        appInfo = AppInfoBuilder().fromVariant(
                            variant = this@all,
                            defaultVersionCode = appExtension.defaultConfig.versionCode ?: 0,
                            defaultVersionName = appExtension.defaultConfig.versionName ?: ""
                        )
                        println("${this.name}: Configuring previous task ${addAppIconTask.name}")
                        dependsOn(addAppIconTask)
                        mustRunAfter(addAppIconTask)
                    }

                addPrintFilesTask(
                    project = project,
                    taskName = "printPreAddIconAssets",
                    variantName = name.capitalize(Locale.ROOT),
                    previousTask = mergeAssetsTask,
                    nextTask = addAppIconTask,
                    directoryToPrint = this.mergeAssetsProvider.get().outputDir.asFile.get()
                )
                addPrintFilesTask(
                    project = project,
                    taskName = "printPostAddInfoAssets",
                    variantName = name.capitalize(Locale.ROOT),
                    previousTask = addAppInfoTask,
                    nextTask = compressAssetsTask,
                    directoryToPrint = this.mergeAssetsProvider.get().outputDir.asFile.get()
                )
                addPrintFilesTask(
                    project = project,
                    taskName = "printPostCompressAssets",
                    variantName = name.capitalize(Locale.ROOT),
                    previousTask = compressAssetsTask,
                    nextTask = processResourcesTask,
                    directoryToPrint = this.mergeAssetsProvider.get().outputDir.asFile.get()
                )
            }
        }
    }

    private fun addPrintFilesTask(
        project: Project,
        variantName: String,
        taskName: String,
        previousTask: Task?,
        nextTask: Task?,
        directoryToPrint: File
    ) {
        val customTaskName = "$taskName$variantName"
        val customTask =
            project.tasks.create(customTaskName, PrintFilesInDirectoryTask::class.java) {
                group = "AppInfo"
                directory = directoryToPrint.absolutePath
            }
        if (previousTask != null) {
            customTask.configure(closureOf<Task> {
                println("$customTaskName: Configuring previous task ${previousTask.name}")
                dependsOn(previousTask)
                mustRunAfter(previousTask)
            })
        }
        nextTask?.configure(closureOf<Task> {
            println("$customTaskName: Configuring next task ${nextTask.name}")
            dependsOn(customTask)
            mustRunAfter(customTask)
        })
    }

    private fun searchForAppIcon(variant: BaseVariant): File? {
        var appIcon: File? = null
        try {
            variant.sourceSets.forEach { sourceProvider ->
                if (appIcon == null) {
                    println("----------------------------------")
                    println("Source set: ${sourceProvider.name}")

                    if (appIcon == null) {
                        println("---")
                        println("Assets directories")
                        sourceProvider.assetsDirectories.forEach { file ->
                            if (appIcon == null) {
                                println(file.absolutePath)
                                appIcon = searchForAppIcon(file)
                            }
                        }
                    }

                    if (appIcon == null) {
                        println("---")
                        println("Kotlin directories")
                        sourceProvider.kotlinDirectories.forEach { file ->
                            if (appIcon == null) {
                                println(file.absolutePath)
                                appIcon = searchForAppIcon(file)
                            }
                        }
                    }

                    if (appIcon == null) {
                        println("---")
                        println("Java directories")
                        sourceProvider.javaDirectories.forEach { file ->
                            if (appIcon == null) {
                                println(file.absolutePath)
                                appIcon = searchForAppIcon(file)
                            }
                        }
                    }

                    if (appIcon == null) {
                        println("---")
                        println("Res directories")
                        sourceProvider.resDirectories.forEach { file ->
                            if (appIcon == null) {
                                println(file.absolutePath)
                                appIcon = searchForAppIcon(file)
                            }
                        }
                    }

                    if (appIcon == null) {
                        println("---")
                        println("Custom directories")
                        sourceProvider.customDirectories.forEach { file ->
                            if (appIcon == null) {
                                println(file.directory.absolutePath)
                                appIcon = searchForAppIcon(file.directory)
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return appIcon
    }

    private fun searchForAppIcon(directory: File): File? {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) return searchForAppIcon(file)
            else if (file.name == APP_ICON_NAME) return file
        }
        return null
    }

    private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
        return getByType(type.java)
    }

    companion object {
        internal const val TAG = "AppInfoPlugin"
    }
}