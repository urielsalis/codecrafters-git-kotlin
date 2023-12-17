package com.urielsalis.codecrafters.git

import java.nio.ByteBuffer

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

fun String.hexToByteArray() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
