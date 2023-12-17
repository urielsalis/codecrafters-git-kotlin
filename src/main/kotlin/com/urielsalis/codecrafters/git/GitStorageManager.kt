package com.urielsalis.codecrafters.git

import com.urielsalis.codecrafters.git.domain.GitObjectType
import com.urielsalis.codecrafters.git.domain.GitTreeEntry
import com.urielsalis.codecrafters.git.domain.RawGitObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.zip.DeflaterInputStream
import java.util.zip.InflaterInputStream

class GitStorageManager(rootDirectory: File) {
    val gitDirectory: File

    init {
        gitDirectory = File(rootDirectory, ".git")
    }

    fun initRepository() {
        File(gitDirectory, "objects").mkdirs()
        File(gitDirectory, "refs").mkdirs()
    }

    fun setHead(head: String) {
        with(File(gitDirectory, "HEAD")) {
            if (!exists()) {
                createNewFile()
            }
            writeText("$head\n")
        }
    }

    fun getType(hash: String): GitObjectType {
        val file = getObjectFile(hash)
        val decompressedData = decompress(file)
        val buffer = ByteBuffer.wrap(decompressedData)
        return GitObjectType.from(String(buffer.takeUntilAndSkip(' '.code.toByte())))
    }

    fun getObject(hash: String): RawGitObject {
        val file = getObjectFile(hash)
        val decompressedData = decompress(file)
        val buffer = ByteBuffer.wrap(decompressedData)
        val type = GitObjectType.from(String(buffer.takeUntilAndSkip(' '.code.toByte())))
        val length = String(buffer.takeUntilAndSkip(0.toByte())).toInt()
        val content = buffer.getNext(length)
        return RawGitObject(type, content)
    }

    fun writeObject(obj: RawGitObject): String {
        val hash = getObjectHash(obj)
        writeObject(hash, obj)
        return hash
    }

    fun writeObject(
        hash: String,
        obj: RawGitObject,
    ) {
        val file = getObjectFile(hash)
        file.parentFile.mkdirs()
        compress(getDiskRepresentation(obj), file)
    }

    fun getObjectHash(obj: RawGitObject): String {
        val sha1 = MessageDigest.getInstance("SHA-1")
        sha1.reset()
        sha1.update(getDiskRepresentation(obj))
        return sha1.digest().toHexString()
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

    private fun getDiskRepresentation(obj: RawGitObject): ByteArray =
        "${obj.type.name.lowercase()} ${obj.content.size}\u0000".toByteArray() + obj.content

    private fun decompress(file: File): ByteArray {
        val inflaterInputStream = InflaterInputStream(file.inputStream())
        val out = ByteArrayOutputStream()
        inflaterInputStream.transferTo(out)
        return out.toByteArray()
    }

    private fun compress(
        data: ByteArray,
        out: File,
    ) {
        val deflaterInputStream = DeflaterInputStream(data.inputStream())
        deflaterInputStream.transferTo(out.outputStream())
    }

    private fun getObjectFile(hash: String): File {
        val prefix = hash.substring(0, 2)
        val suffix = hash.substring(2)
        return File(gitDirectory, "objects/$prefix/$suffix")
    }
}
