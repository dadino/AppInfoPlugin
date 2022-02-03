package com.appinfoplugin

import com.appinfoplugin.AppInfoPlugin.Companion.TAG
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File


open class AddAppInfoTask : DefaultTask() {
    @get:Input
    lateinit var appInfo: AppInfo

    @get:Input
    lateinit var appInfoBuildDirPath: String

    @get:Input
    lateinit var assetsDirPath: String

    private val jsonEncoder = Json {
        prettyPrint = true
    }

    @TaskAction
    fun addAppInfo() {
        println("$TAG -> appInfo: $appInfo")
        println("$TAG -> appInfoBuildDir path: $appInfoBuildDirPath")
        println("$TAG -> assetsDir path: $assetsDirPath")

        //Create file
        val appInfoBuildDir = File(appInfoBuildDirPath)
        appInfoBuildDir.mkdirs()
        val jsonFile = File(appInfoBuildDir, appInfoFileName)

        val json = jsonEncoder.encodeToString(appInfo)
        println("$TAG -> $json")

        //Write json in file
        jsonFile.writeText(json)

        //Copy file into apk
        val rawAssetsDirPath = "$assetsDirPath/raw/"
        println("$TAG -> coping from ${appInfoBuildDir.absolutePath} to $rawAssetsDirPath")
        project.copy {
            from(appInfoBuildDir.absolutePath)
            into(rawAssetsDirPath)
        }

        //delete file
        println("$TAG -> deleting path ${appInfoBuildDir.absolutePath}")
        project.delete {
            delete(appInfoBuildDir.absolutePath)
        }
        println("$TAG -> delete completed for path ${appInfoBuildDir.absolutePath}")
    }

    companion object {
        private const val appInfoFileName = "app_info.json"

    }

}