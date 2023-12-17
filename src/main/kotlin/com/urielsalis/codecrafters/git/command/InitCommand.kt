package com.urielsalis.codecrafters.git.command

import com.urielsalis.codecrafters.git.GitFolderManager
import picocli.CommandLine.Command
import java.io.File
import java.util.concurrent.Callable

@Command(name = "init")
class InitCommand : Callable<Unit> {
    override fun call() {
        val manager = GitFolderManager(File("."))
        manager.initRepository()
        manager.setHead("ref: refs/heads/master")
    }
}
