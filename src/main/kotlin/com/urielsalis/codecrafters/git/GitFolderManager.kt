package com.urielsalis.codecrafters.git

import java.io.File

class GitFolderManager(rootDirectory: File) {
    val gitDirectory: File

    init {
        gitDirectory = File(rootDirectory, ".git")
    }

    fun initRepository() {
        File(gitDirectory, "objects").mkdirs()
        File(gitDirectory, "refs").mkdirs()
    }

    fun setHead(head: String) {
        with(File(gitDirectory, "HEAD")) {
            if (!exists()) {
                createNewFile()
            }
            writeText("$head\n")
        }
    }
}
