package com.urielsalis.codecrafters.git.domain

sealed class GitObject

data class GitBlobObject(val content: ByteArray) : GitObject() {
    override fun toString(): String {
        return String(content)
    }
}
