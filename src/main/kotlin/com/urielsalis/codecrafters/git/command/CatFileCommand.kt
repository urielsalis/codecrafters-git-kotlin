package com.urielsalis.codecrafters.git.command

import com.urielsalis.codecrafters.git.GitObjectManager
import com.urielsalis.codecrafters.git.GitStorageManager
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.util.concurrent.Callable

@Command(name = "cat-file")
class CatFileCommand : Callable<Unit> {
    @Parameters(index = "0", description = ["Hash of the object to display"])
    private lateinit var objectHash: String

    @Option(names = ["-p"], description = ["Pretty print the result"])
    private var prettyPrint: Boolean = false

    override fun call() {
        if (!prettyPrint) {
            TODO("Only pretty print is supported")
        }
        val storage = GitStorageManager(File("."))
        val parser = GitObjectManager()
        val obj = storage.getObject(objectHash)
        val parsedObj = parser.parse(objectHash, obj)
        print(parsedObj)
    }
}
