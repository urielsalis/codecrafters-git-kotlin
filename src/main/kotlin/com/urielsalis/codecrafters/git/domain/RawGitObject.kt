package com.urielsalis.codecrafters.git.domain

data class RawGitObject(val type: GitObjectType, val content: ByteArray)
