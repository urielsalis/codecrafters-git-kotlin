package com.urielsalis.codecrafters.git

import com.urielsalis.codecrafters.git.domain.GitBlobObject
import com.urielsalis.codecrafters.git.domain.GitObject
import com.urielsalis.codecrafters.git.domain.GitObjectType
import com.urielsalis.codecrafters.git.domain.RawGitObject

class GitObjectManager {
    fun parse(
        hash: String,
        obj: RawGitObject,
    ): GitObject {
        when (obj.type) {
            GitObjectType.BLOB -> return GitBlobObject(hash, obj.content)
            else -> TODO("Parsing not yet implemented")
        }
    }
}
