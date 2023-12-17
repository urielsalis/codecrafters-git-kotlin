package com.urielsalis.codecrafters.git.command

import com.urielsalis.codecrafters.git.GitObjectManager
import com.urielsalis.codecrafters.git.GitStorageManager
import com.urielsalis.codecrafters.git.domain.GitTreeObject
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.util.concurrent.Callable

@Command(name = "ls-tree")
class LsTreeCommand : Callable<Unit> {
    @Parameters(index = "0", description = ["Hash of the tree to display"])
    private lateinit var treeHash: String

    @Option(names = ["--name-only"], description = ["Only show the names of the entries"])
    private var nameOnly: Boolean = false

    override fun call() {
        val storage = GitStorageManager(File("."))
        val parser = GitObjectManager()
        val obj = storage.getObject(treeHash)
        val parsedObj = parser.parse(treeHash, obj)
        if (parsedObj !is GitTreeObject) {
            throw IllegalArgumentException("Not a tree")
        }
        if (nameOnly) {
            println(parsedObj.entries.joinToString("\n") { it.name })
        } else {
            println(
                parsedObj.entries.joinToString("\n") {
                    val mode = it.mode.padStart(6, '0')
                    val type = storage.getType(it.hash).name.lowercase()
                    "$mode $type ${it.hash}\t${it.name}"
                },
            )
        }
    }
}
