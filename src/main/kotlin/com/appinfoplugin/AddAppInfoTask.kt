package com.appinfoplugin

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File


abstract class AddAppInfoTask : DefaultTask() {

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val versionName: Property<String>

    @get:Input
    abstract val versionCode: Property<Int>

    @get:Input
    abstract val environment: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private val jsonEncoder = Json { prettyPrint = true }

    @TaskAction
    fun addAppInfo() {
        val appInfo = AppInfo(
            packageName = packageName.get(),
            versionName = versionName.getOrElse(""),
            versionCode = versionCode.getOrElse(0),
            environment = environment.get()
        )

        val json = jsonEncoder.encodeToString(appInfo)
        println("${AppInfoPlugin.TAG} -> Generated JSON: \n$json")

        val jsonFile = File(outputDir.asFile.get(), "app_info.json")
        jsonFile.parentFile.mkdirs()
        jsonFile.writeText(json)
    }
}