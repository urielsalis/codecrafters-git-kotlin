package com.urielsalis.codecrafters.git.domain

sealed class GitProtocolObject

data class NormalGitProtocolObject(val type: GitProtocolObjectType, val content: ByteArray) :
    GitProtocolObject()

data class DeltaGitProtocolObject(val baseHash: String, val content: ByteArray) :
    GitProtocolObject()
