package com.urielsalis.codecrafters.git

import com.urielsalis.codecrafters.git.domain.GitBlobObject
import com.urielsalis.codecrafters.git.domain.GitObject
import com.urielsalis.codecrafters.git.domain.GitObjectType
import com.urielsalis.codecrafters.git.domain.RawGitObject

class GitObjectManager {
    fun parse(obj: RawGitObject): GitObject {
        when (obj.type) {
            GitObjectType.BLOB -> return GitBlobObject(obj.content)
            else -> TODO("Parsing not yet implemented")
        }
    }
}
