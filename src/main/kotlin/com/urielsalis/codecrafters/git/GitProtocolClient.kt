package com.urielsalis.codecrafters.git

import com.urielsalis.codecrafters.git.domain.DataPacketLine
import com.urielsalis.codecrafters.git.domain.DeltaGitProtocolObject
import com.urielsalis.codecrafters.git.domain.FlushPacketLine
import com.urielsalis.codecrafters.git.domain.GitPacketLine
import com.urielsalis.codecrafters.git.domain.GitProtocolObject
import com.urielsalis.codecrafters.git.domain.GitProtocolObjectType
import com.urielsalis.codecrafters.git.domain.GitReference
import com.urielsalis.codecrafters.git.domain.NormalGitProtocolObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer

class GitProtocolClient(val url: String) {
    val client: HttpClient

    init {
        client = HttpClient.newBuilder().build()
    }

    fun getReferences(): List<GitReference> {
        val request =
            HttpRequest.newBuilder().uri(URI.create("$url/info/refs?service=git-upload-pack")).GET()
                .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        val packetLines = parsePacketLines(response.body())

        return packetLines.subList(1, packetLines.size - 1).filterIsInstance<DataPacketLine>().map {
            val split = it.data.split(" ")
            GitReference(split[0], split[1].substringBefore('\u0000'))
        }
    }

    private fun parsePacketLines(body: ByteArray): List<GitPacketLine> {
        val result = mutableListOf<GitPacketLine>()
        val buffer = ByteBuffer.wrap(body)
        while (buffer.hasRemaining()) {
            val lengthRaw = buffer.getNext(4)
            val length = Integer.parseInt(String(lengthRaw), 16)
            if (length == 0) {
                result.add(FlushPacketLine)
            } else {
                val line = buffer.getNext(length - 4)
                val lineString = String(line)
                result.add(DataPacketLine(lineString.substring(0, lineString.length - 1)))
            }
        }
        return result
    }

    fun downloadReferences(referenceHashes: Set<String>): List<GitProtocolObject> {
        val packetContent =
            referenceHashes.joinToString("\n") { "0032want $it\n" } + "00000009done\n"
        val request =
            HttpRequest.newBuilder().uri(URI.create("$url/git-upload-pack"))
                .header("Content-Type", "application/x-git-upload-pack-request")
                .POST(HttpRequest.BodyPublishers.ofString(packetContent)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        val buffer = ByteBuffer.wrap(response.body())
        assert(String(buffer.getNext(8)) != "0008NAK\n") { "Invalid response from server, didn't contain NAK" }
        return parsePack(buffer)
    }

    private fun parsePack(buffer: ByteBuffer): List<GitProtocolObject> {
        assert(String(buffer.getNext(4)) != "PACK") { "Invalid response from server, didn't contain PACK" }
        val version = buffer.getInt()
        assert(version == 2 || version == 3) { "Invalid response from server, version was $version" }
        val numberOfObjects = buffer.getInt()

        val objects = mutableListOf<GitProtocolObject>()
        for (i in 0 until numberOfObjects) {
            objects.add(parseObject(buffer))
        }
        return objects
    }

    private fun parseObject(buffer: ByteBuffer): GitProtocolObject {
        val typeAndSize = buffer.get().toUByte()
        val type = (typeAndSize and 0x70u).toUInt() shr 4
        if ((typeAndSize and 0x80u) != 0.toUByte()) {
            readSizeEncoding(buffer)
        }
        val typeEnum = GitProtocolObjectType.fromValue(type.toByte())
        require(typeEnum != GitProtocolObjectType.OFS_DELTA) { "OFS_DELTA not supported yet" }
        val hash =
            if (typeEnum == GitProtocolObjectType.REF_DELTA) {
                buffer.getNext(20).toHexString()
            } else {
                null
            }
        val dataUncompressed = buffer.decompress()
        if (hash != null) {
            return DeltaGitProtocolObject(hash, dataUncompressed)
        } else {
            return NormalGitProtocolObject(typeEnum, dataUncompressed)
        }
    }

    private fun readSizeEncoding(buffer: ByteBuffer): Int {
        var read = buffer.get().toUByte()
        var value = (read and 0x0fu).toInt()
        while (read and 0b1000_0000u != 0.toUByte()) {
            value = value or ((read and 0x7fu).toInt() shl 7)
            read = buffer.get().toUByte()
        }
        return value
    }

    fun parseDelta(
        base: ByteArray,
        instructions: ByteArray,
    ): ByteArray {
        val buffer = ByteBuffer.wrap(instructions)
        readObjSizeDelta(buffer)
        val finalObjectSize = readObjSizeDelta(buffer)
        val outputBuffer = ByteBuffer.allocate(finalObjectSize)
        while (buffer.hasRemaining()) {
            val read = buffer.get().toUByte()
            if (read and 0b1000_0000u != 0.toUByte()) {
                // Copy
                val determinant = read
                val hasOffset1 = (determinant and 0b0000_0001u) != 0.toUByte()
                val hasOffset2 = (determinant and 0b0000_0010u) != 0.toUByte()
                val hasOffset3 = (determinant and 0b0000_0100u) != 0.toUByte()
                val hasOffset4 = (determinant and 0b0000_1000u) != 0.toUByte()
                val hasCopySize1 = (determinant and 0b0001_0000u) != 0.toUByte()
                val hasCopySize2 = (determinant and 0b0010_0000u) != 0.toUByte()
                val hasCopySize3 = (determinant and 0b0100_0000u) != 0.toUByte()
                val offset = parseCopyNumber(buffer, hasOffset1, hasOffset2, hasOffset3, hasOffset4)
                val size = parseCopyNumber(buffer, hasCopySize1, hasCopySize2, hasCopySize3)
                val toCopy = ByteArray(size)
                System.arraycopy(base, offset, toCopy, 0, size)
                outputBuffer.put(toCopy)
            } else {
                // Insert
                val size = read and 0b0111_1111u
                val toInsert = ByteArray(size.toInt())
                buffer.get(toInsert)
                outputBuffer.put(toInsert)
            }
        }
        val data = ByteArray(outputBuffer.position())
        outputBuffer.rewind().get(data)
        return data
    }

    private fun parseCopyNumber(
        buffer: ByteBuffer,
        vararg bits: Boolean,
    ): Int {
        val parts = bits.map { if (it) buffer.get() else 0 }
        return parts.indices.sumOf { parts[it].toUByte().toInt() shl (it * 8) }
    }

    private fun readObjSizeDelta(buffer: ByteBuffer): Int {
        var read = buffer.get().toUByte()
        var value = (read and 0b01111111u).toInt()
        while (read and 0b10000000u != 0.toUByte()) {
            read = buffer.get().toUByte()
            value = (value shl 7) + (read and 0b01111111u).toInt()
        }
        return value
    }
}
