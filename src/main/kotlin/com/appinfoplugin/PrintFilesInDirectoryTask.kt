package com.appinfoplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File


open class PrintFilesInDirectoryTask : DefaultTask() {

    @get:Input
    lateinit var directory: String


    @TaskAction
    fun checkAssets() {
        println("$name -> directory path: $directory")

        val file = File(directory)
        printFiles(file)
    }

    private fun printFiles(directory: File) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) printFiles(file)
            else {
                println("[${directory.name}] -> ${file.name}")
            }
        }
    }
}