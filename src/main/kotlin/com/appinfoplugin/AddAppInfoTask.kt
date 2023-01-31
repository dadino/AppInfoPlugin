package com.appinfoplugin

import com.appinfoplugin.AppInfoPlugin.Companion.TAG
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File


abstract class AddAppInfoTask : DefaultTask() {
    @get:Input
    lateinit var buildNumber: String

    @get:Input
    lateinit var appInfo: AppInfo

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private val jsonEncoder = Json {
        prettyPrint = true
    }

    @TaskAction
    fun addAppInfo() {
        println("$name -> appInfo: $appInfo")

        //Create file
        val json = jsonEncoder.encodeToString(appInfo)
        println("$TAG -> $json")

        //Copy file into apk
        val jsonFile = File(outputDir.asFile.get(), appInfoFileName)
        println("$name -> appInfo json file path: ${jsonFile.path}")
        jsonFile.parentFile.mkdirs()
        //Write json in file
        jsonFile.writeText(json)


        //  println("$TAG -> coping from ${appInfoBuildDir.absolutePath} to $assetsDirPath, there are ${appInfoBuildDir.listFiles()?.size} files")
//
        //  project.copy {
        //      from(appInfoBuildDir.absolutePath)
        //      into(assetsDirPath)
        //  }
    }

    companion object {
        private const val appInfoFileName = "app_info.json"

    }

}