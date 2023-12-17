package com.urielsalis.codecrafters.git.command

import com.urielsalis.codecrafters.git.GitObjectManager
import com.urielsalis.codecrafters.git.GitStorageManager
import com.urielsalis.codecrafters.git.domain.GitObjectType
import com.urielsalis.codecrafters.git.domain.GitTreeEntry
import com.urielsalis.codecrafters.git.domain.RawGitObject
import picocli.CommandLine.Command
import java.io.File
import java.util.concurrent.Callable

@Command(name = "write-tree")
class WriteTreeCommand : Callable<Unit> {
    override fun call() {
        val storage = GitStorageManager(File("."))
        val parser = GitObjectManager()
        val hash = writeTreeInternal(storage, parser, File("."))
        println(hash)
    }

    private fun writeTreeInternal(
        storage: GitStorageManager,
        parser: GitObjectManager,
        dir: File,
    ): String {
        val entries =
            dir.listFiles()?.filter { !it.name.startsWith(".") }?.map {
                if (it.isDirectory) {
                    val hash = writeTreeInternal(storage, parser, it)
                    GitTreeEntry("40000", it.name, hash)
                } else {
                    val contents = it.readBytes()
                    val rawObject = RawGitObject(GitObjectType.BLOB, contents)
                    val hash = storage.writeObject(rawObject)
                    if (it.canExecute()) {
                        GitTreeEntry("100755", it.name, hash)
                    } else {
                        GitTreeEntry("100644", it.name, hash)
                    }
                }
            } ?: emptyList()
        val tree = storage.makeTree(entries)
        val hash = storage.writeObject(tree)
        return hash
    }
}
