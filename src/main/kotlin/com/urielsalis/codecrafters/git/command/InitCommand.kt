package com.urielsalis.codecrafters.git.command

import com.urielsalis.codecrafters.git.GitStorageManager
import picocli.CommandLine.Command
import java.io.File
import java.util.concurrent.Callable

@Command(name = "init")
class InitCommand : Callable<Unit> {
    override fun call() {
        val storage = GitStorageManager(File("."))
        storage.initRepository()
        storage.setHead("ref: refs/heads/master")
    }
}
