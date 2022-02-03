package com.appinfoplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.CompressAssetsTask
import kotlinx.serialization.json.Json
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.getByName
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
                val appAppInfoTaskName = "addAppInfo${name.capitalize(Locale.ROOT)}"
                val addAppInfoTask = project.tasks.create(appAppInfoTaskName, AddAppInfoTask::class.java) {
                    println("$TAG -> Configuring task $appAppInfoTaskName")
                    group = "AppInfo"
                    appInfoBuildDirPath = getBuildDirForAppInfo(project, this@all)
                    appInfo = AppInfoBuilder().fromVariant(
                        variant = this@all,
                        defaultVersionCode = appExtension.defaultConfig.versionCode ?: 0,
                        defaultVersionName = appExtension.defaultConfig.versionName ?: ""
                    )
                    assetsDirPath = this@all.mergeAssetsProvider.get().outputDir.asFile.get().absolutePath
                }

                this.mergeAssetsProvider.configure {
                    finalizedBy(addAppInfoTask)
                }

                project.tasks.getByName("compress${name.capitalize(Locale.ROOT)}Assets", CompressAssetsTask::class) {
                    doFirst {
                        println("$TAG -> doFirst for $name")
                        val inputDir = File(inputDirs.asPath)
                        inputDir.walkTopDown().forEach { file ->
                            println("$TAG -> Input file: $file")
                        }
                    }
                    doLast {
                        println("$TAG -> doLast for $name")
                        val outputDir = outputDir.asFile.get()
                        outputDir.walkTopDown().forEach { file ->
                            println("$TAG -> Output file: $file")
                        }
                    }
                    outputs.upToDateWhen { false }
                    dependsOn(addAppInfoTask)
                }

            }
        }
    }

    private fun getBuildDirForAppInfo(project: Project, baseVariant: BaseVariant): String {
        return "${project.buildDir}/generated/source/appInfo/${baseVariant.name}"
    }

    private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
        return getByType(type.java)
    }

    companion object {
        internal const val TAG = "AppInfoPlugin"
    }
}