package com.urielsalis.codecrafters.git

import com.urielsalis.codecrafters.git.domain.GitObjectType
import com.urielsalis.codecrafters.git.domain.RawGitObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.InflaterInputStream

class GitFolderManager(rootDirectory: File) {
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

    fun getObject(objectHash: String): RawGitObject {
        val prefix = objectHash.substring(0, 2)
        val suffix = objectHash.substring(2)
        val file = File(gitDirectory, "objects/$prefix/$suffix")
        val decompressedData = decompress(file)
        val buffer = ByteBuffer.wrap(decompressedData)
        val type = GitObjectType.from(String(buffer.takeUntilAndSkip(' '.code.toByte())))
        val length = String(buffer.takeUntilAndSkip(0.toByte())).toInt()
        val content = buffer.getNext(length)
        return RawGitObject(type, content)
    }

    fun decompress(file: File): ByteArray {
        val inflaterInputStream = InflaterInputStream(file.inputStream())
        val out = ByteArrayOutputStream()
        inflaterInputStream.transferTo(out)
        return out.toByteArray()
    }
}
