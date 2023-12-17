package com.urielsalis.codecrafters.git.domain

sealed class GitObject(open val hash: String)

data class GitBlobObject(override val hash: String, val content: ByteArray) : GitObject(hash) {
    override fun toString(): String {
        return String(content)
    }
}

data class GitTreeObject(override val hash: String, val entries: List<GitTreeEntry>) :
    GitObject(hash) {
    override fun toString(): String {
        return entries.joinToString("\n")
    }
}

data class GitTreeEntry(val mode: String, val name: String, val hash: String) {
    override fun toString(): String {
        return "$mode $name\t$hash"
    }
}
