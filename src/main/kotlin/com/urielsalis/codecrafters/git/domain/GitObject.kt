package com.urielsalis.codecrafters.git.domain

sealed class GitObject(open val hash: String)

data class GitBlobObject(override val hash: String, val content: ByteArray) : GitObject(hash) {
    override fun toString(): String {
        return String(content)
    }
}
