package com.appinfoplugin

import com.appinfoplugin.AppInfoPlugin.Companion.TAG
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


abstract class AddAppIconTask : DefaultTask() {

    @get:InputFile
    @get:Optional
    abstract val appIcon: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun addAppInfo() {
        val appIconfile = appIcon.orNull?.asFile
        if (appIconfile != null) {
            println("$TAG -> appIcon path: ${appIconfile.absolutePath}")

            val assetsDir = outputDir.asFile.get()
            println("$TAG -> coping from $appIconfile to ${assetsDir.absolutePath}")

            project.copy {
                from(appIconfile.absolutePath)
                into(assetsDir.absolutePath)
            }
        }
    }

    companion object {
        internal const val APP_ICON_NAME = "app_icon.png"
    }

}