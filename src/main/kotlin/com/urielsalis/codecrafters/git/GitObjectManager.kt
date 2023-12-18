package com.urielsalis.codecrafters.git

import com.urielsalis.codecrafters.git.domain.GitBlobObject
import com.urielsalis.codecrafters.git.domain.GitCommitObject
import com.urielsalis.codecrafters.git.domain.GitObjectType
import com.urielsalis.codecrafters.git.domain.GitTreeEntry
import com.urielsalis.codecrafters.git.domain.GitTreeObject
import com.urielsalis.codecrafters.git.domain.RawGitObject
import java.nio.ByteBuffer

class GitObjectManager {
    fun parse(
        hash: String,
        obj: RawGitObject,
    ) = when (obj.type) {
        GitObjectType.BLOB -> GitBlobObject(hash, obj.content)
        GitObjectType.TREE -> parseTree(hash, obj.content)
        GitObjectType.COMMIT -> parseCommit(hash, obj.content)
        else -> TODO("Parsing not yet implemented")
    }

    fun makeTree(entries: List<GitTreeEntry>): RawGitObject {
        val entriesRaw =
            entries.sortedBy { it.name }
                .map { "${it.mode} ${it.name}\u0000".toByteArray() + it.hash.hexToByteArray() }
        if (entriesRaw.isEmpty()) {
            return RawGitObject(GitObjectType.TREE, byteArrayOf())
        }
        return RawGitObject(GitObjectType.TREE, entriesRaw.reduce { acc, bytes -> acc + bytes })
    }

    fun makeCommit(
        treeHash: String,
        message: String,
        parameters: Map<String, String>,
    ): RawGitObject {
        val content =
            mutableListOf(
                "tree $treeHash",
                *parameters.map { (key, value) -> "$key $value" }.toTypedArray(),
                "",
                message,
            ).joinToString("\n").toByteArray()
        return RawGitObject(GitObjectType.COMMIT, content)
    }

    private fun parseTree(
        hash: String,
        content: ByteArray,
    ): GitTreeObject {
        val entries = mutableListOf<GitTreeEntry>()
        val buffer = ByteBuffer.wrap(content)
        while (buffer.hasRemaining()) {
            val mode = String(buffer.takeUntilAndSkip(' '.code.toByte()))
            val name = String(buffer.takeUntilAndSkip(0.toByte()))
            val hash = buffer.getNext(20).toHexString()
            entries.add(GitTreeEntry(mode, name, hash))
        }
        return GitTreeObject(hash, entries)
    }

    private fun parseCommit(
        hash: String,
        content: ByteArray,
    ): GitCommitObject {
        val buffer = ByteBuffer.wrap(content)
        val parameters = mutableMapOf<String, String>()
        while (buffer.hasRemaining()) {
            val line = String(buffer.takeUntilAndSkip('\n'.code.toByte()))
            if (line.isEmpty()) {
                break
            }
            val (key, value) = line.split(" ", limit = 2)
            parameters[key] = value
        }
        val message = String(buffer.getNext(buffer.remaining()))
        val tree = parameters["tree"] ?: throw IllegalArgumentException("No tree")
        val parent = parameters["parent"]
        parameters.remove("tree")
        parameters.remove("parent")
        return GitCommitObject(hash, tree, parent, message, parameters)
    }
}
