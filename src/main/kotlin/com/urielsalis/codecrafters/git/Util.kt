package com.urielsalis.codecrafters.git

import java.nio.ByteBuffer
import java.util.zip.Inflater

fun ByteBuffer.takeUntilAndSkip(toByte: Byte): ByteArray {
    val out = ByteArray(remaining())
    var i = 0
    while (hasRemaining()) {
        val b = get()
        if (b == toByte) {
            break
        }
        out[i++] = b
    }
    return out.copyOf(i)
}

fun ByteBuffer.getNext(length: Int): ByteArray =
    ByteArray(length).apply {
        get(this)
    }

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun ByteBuffer.decompress(): ByteArray {
    val dataBuffer = slice()
    val inflater = Inflater()
    inflater.setInput(dataBuffer)

    val outputBuffer = ByteBuffer.allocate(1024 * 1024)
    inflater.inflate(outputBuffer)

    val dataDecompressed = ByteArray(inflater.totalOut)
    outputBuffer.rewind().get(dataDecompressed)

    position(position() + inflater.totalIn)
    return dataDecompressed
}

fun String.hexToByteArray() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
