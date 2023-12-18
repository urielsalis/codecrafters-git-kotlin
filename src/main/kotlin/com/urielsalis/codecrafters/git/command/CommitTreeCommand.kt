package com.urielsalis.codecrafters.git.command

import com.urielsalis.codecrafters.git.GitObjectManager
import com.urielsalis.codecrafters.git.GitStorageManager
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.time.Instant
import java.util.concurrent.Callable

@Command(name = "commit-tree")
class CommitTreeCommand : Callable<Unit> {
    @Parameters(index = "0", description = ["Hash of the tree"])
    private lateinit var treeHash: String

    @Option(names = ["-p"], description = ["Parent commit"])
    private var parentCommit: String? = null

    @Option(names = ["-m"], description = ["Message"])
    private lateinit var message: String

    override fun call() {
        val storage = GitStorageManager(File("."))
        val parser = GitObjectManager()
        val currentTime = Instant.now().epochSecond
        val parameters =
            mutableMapOf(
                "author" to "Uriel Salischiker <uriel@urielsalis.com> $currentTime +0000",
                "committer" to "Uriel Salischiker <uriel@urielsalis.com> $currentTime +0000",
            )
        if (parentCommit != null) {
            parameters["parent"] = parentCommit!!
        }
        val commit = parser.makeCommit(treeHash, message + "\n", parameters)
        val hash = storage.getObjectHash(commit)
        storage.writeObject(hash, commit)
        println(hash)
    }
}
