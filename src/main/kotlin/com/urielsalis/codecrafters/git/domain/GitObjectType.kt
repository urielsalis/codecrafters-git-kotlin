package com.urielsalis.codecrafters.git.domain

enum class GitObjectType {
    BLOB,
    TREE,
    COMMIT,
    TAG,
    ;

    companion object {
        fun from(str: String): GitObjectType = GitObjectType.valueOf(str.uppercase())
    }
}
