package com.urielsalis.codecrafters.git.command

import com.urielsalis.codecrafters.git.GitObjectManager
import com.urielsalis.codecrafters.git.GitProtocolClient
import com.urielsalis.codecrafters.git.GitStorageManager
import com.urielsalis.codecrafters.git.domain.DeltaGitProtocolObject
import com.urielsalis.codecrafters.git.domain.GitCommitObject
import com.urielsalis.codecrafters.git.domain.GitObjectType
import com.urielsalis.codecrafters.git.domain.GitProtocolObjectType
import com.urielsalis.codecrafters.git.domain.GitTreeObject
import com.urielsalis.codecrafters.git.domain.NormalGitProtocolObject
import com.urielsalis.codecrafters.git.domain.RawGitObject
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.io.File
import java.util.concurrent.Callable

@Command(name = "clone")
class CloneCommand : Callable<Unit> {
    @Parameters(index = "0", description = ["URL to clone"])
    private lateinit var url: String

    @Parameters(index = "1", description = ["Destination directory"])
    private lateinit var dir: File

    override fun call() {
        val storage = GitStorageManager(dir)
        val parser = GitObjectManager()
        val client = GitProtocolClient(url)

        storage.initRepository()
        val references = client.getReferences()
        references.forEach {
            storage.writeReference(it)
        }

        val referenceHashes = references.map { it.hash }.toSet()
        val objects = client.downloadReferences(referenceHashes)

        val headReference = references.find { it.name == "HEAD" }!!
        val sameHashAsHeadReference =
            references.filter { it.name != "HEAD" }.firstOrNull { it.hash == headReference.hash }
        if (sameHashAsHeadReference == null) {
            storage.setHead(headReference.hash)
        } else {
            storage.setHead("ref: ${sameHashAsHeadReference.name}")
        }

        objects.filterIsInstance<NormalGitProtocolObject>().forEach {
            val type =
                when (it.type) {
                    GitProtocolObjectType.COMMIT -> GitObjectType.COMMIT
                    GitProtocolObjectType.TREE -> GitObjectType.TREE
                    GitProtocolObjectType.BLOB -> GitObjectType.BLOB
                    GitProtocolObjectType.TAG -> GitObjectType.TAG
                    else -> throw Exception("Unknown type")
                }
            storage.writeObject(RawGitObject(type, it.content))
        }
        objects.filterIsInstance<DeltaGitProtocolObject>().forEach {
            val base = storage.getObject(it.baseHash)
            val newFile = client.parseDelta(base.content, it.content)
            storage.writeObject(RawGitObject(base.type, newFile))
        }

        val commit = parser.parse(headReference.hash, storage.getObject(headReference.hash))
        check(commit is GitCommitObject) { "HEAD is not a commit" }
        val commits = mutableListOf<GitCommitObject>()
        var currentCommit: GitCommitObject = commit
        commits.add(currentCommit)
        while (currentCommit.parent != null) {
            currentCommit =
                parser.parse(
                    currentCommit.parent!!, storage.getObject(currentCommit.parent!!),
                ) as GitCommitObject
            commits.add(currentCommit)
        }
        commits.reversed().forEach {
            val tree = parser.parse(it.tree, storage.getObject(it.tree)) as GitTreeObject
            writeObjectsFromTreeToDisk(dir, tree, storage, parser)
        }
    }

    private fun writeObjectsFromTreeToDisk(
        dir: File,
        tree: GitTreeObject,
        storage: GitStorageManager,
        parser: GitObjectManager,
    ) {
        for (entry in tree.entries) {
            val obj = storage.getObject(entry.hash)
            if (obj.type == GitObjectType.TREE) {
                val newDir = File(dir, entry.name)
                if (!newDir.exists()) {
                    newDir.mkdir()
                }
                val newTree = parser.parse(entry.hash, obj) as GitTreeObject
                writeObjectsFromTreeToDisk(newDir, newTree, storage, parser)
            } else {
                val file = File(dir, entry.name)
                if (!file.exists()) {
                    file.createNewFile()
                }
                file.writeBytes(obj.content)
            }
        }
    }
}
