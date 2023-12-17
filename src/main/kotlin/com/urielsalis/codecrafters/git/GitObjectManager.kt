package com.urielsalis.codecrafters.git

import com.urielsalis.codecrafters.git.domain.GitBlobObject
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
        GitObjectType.TREE -> GitTreeObject(hash, parseTree(obj.content))
        else -> TODO("Parsing not yet implemented")
    }

    private fun parseTree(content: ByteArray): List<GitTreeEntry> {
        val entries = mutableListOf<GitTreeEntry>()
        val buffer = ByteBuffer.wrap(content)
        while (buffer.hasRemaining()) {
            val mode = String(buffer.takeUntilAndSkip(' '.code.toByte()))
            val name = String(buffer.takeUntilAndSkip(0.toByte()))
            val hash = buffer.getNext(20).toHexString()
            entries.add(GitTreeEntry(mode, name, hash))
        }
        return entries
    }
}
