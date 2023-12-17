package com.urielsalis.codecrafters.git.command

import com.urielsalis.codecrafters.git.GitStorageManager
import com.urielsalis.codecrafters.git.domain.GitObjectType
import com.urielsalis.codecrafters.git.domain.RawGitObject
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.util.concurrent.Callable

@Command(name = "hash-object")
class HashObjectCommand : Callable<Unit> {
    @Parameters(index = "0", description = ["File to hash"])
    private lateinit var file: File

    @Option(names = ["-w"], description = ["If the result should be written to the object store"])
    private var write: Boolean = false

    override fun call() {
        val storage = GitStorageManager(File("."))
        val contents = file.readBytes()
        val rawObject = RawGitObject(GitObjectType.BLOB, contents)
        val hash = storage.getObjectHash(rawObject)
        if (write) {
            storage.writeObject(hash, rawObject)
        }
        print(hash)
    }
}
