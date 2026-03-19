package com.appinfoplugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File


abstract class AddAppIconTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val searchDirs: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun addAppIcon() {
        var foundIcon: File? = null

        for (dir in searchDirs.files) {
            foundIcon = searchForAppIcon(dir)
            if (foundIcon != null) break
        }

        if (foundIcon != null) {
            println("${AppInfoPlugin.TAG} -> Found appIcon at: ${foundIcon.absolutePath}")
            val destFile = File(outputDir.asFile.get(), APP_ICON_NAME)
            destFile.parentFile.mkdirs()
            foundIcon.copyTo(destFile, overwrite = true)
        } else {
            println("${AppInfoPlugin.TAG} -> No $APP_ICON_NAME found in source directories.")
        }
    }

    private fun searchForAppIcon(directory: File): File? {
        if (!directory.exists() || !directory.isDirectory) return null

        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                val found = searchForAppIcon(file)
                if (found != null) return found
            } else if (file.name == APP_ICON_NAME) {
                return file
            }
        }
        return null
    }

    companion object {
        internal const val APP_ICON_NAME = "app_icon.png"
    }
}