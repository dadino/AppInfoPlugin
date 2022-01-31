package com.appinfoplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import java.io.File
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
                this.mergeAssetsProvider.configure {
                    doLast {
                        //Create file
                        val appInfoBuildDir = getBuildDirForAppInfo(project, this@all)
                        println("FlavorJsonInApkPlugin -> output file: ${appInfoBuildDir.absolutePath}")
                        appInfoBuildDir.mkdirs()
                        val jsonFile = File(appInfoBuildDir, "app_info.json")

                        //Create AppInfo json
                        val appInfo = AppInfoBuilder().fromVariant(
                            variant = this@all,
                            defaultVersionCode = appExtension.defaultConfig.versionCode ?: 0,
                            defaultVersionName = appExtension.defaultConfig.versionName ?: ""
                        )
                        val json = jsonEncoder.encodeToString(appInfo)
                        println("FlavorJsonInApkPlugin -> $json")

                        //Write json in file
                        jsonFile.writeText(json)

                        //Copy file into apk
                        val apkAssetPath = "${outputDir.asFile.get().absolutePath}/raw/"
                        println("FlavorJsonInApkPlugin -> coping from ${appInfoBuildDir.absolutePath} to $apkAssetPath")
                        project.copy {
                            from(appInfoBuildDir.absolutePath)
                            into(apkAssetPath)
                        }

                        println("FlavorJsonInApkPlugin -> deleting path ${appInfoBuildDir.absolutePath}")

                        //delete file
                        project.delete {
                            delete(appInfoBuildDir.absolutePath)
                        }

                        println("FlavorJsonInApkPlugin -> delete completed for path ${appInfoBuildDir.absolutePath}")
                    }
                }
            }
        }
    }

    private fun getBuildDirForAppInfo(project: Project, baseVariant: BaseVariant) =
        File("${project.buildDir}/generated/source/appInfo/${baseVariant.name}")

    private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
        return getByType(type.java)
    }
}