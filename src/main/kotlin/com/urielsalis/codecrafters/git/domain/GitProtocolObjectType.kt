package com.urielsalis.codecrafters.git.domain

enum class GitProtocolObjectType(val value: Byte) {
    COMMIT(1),
    TREE(2),
    BLOB(3),
    TAG(4),
    OFS_DELTA(6),
    REF_DELTA(7),
    ;

    companion object {
        fun fromValue(type: Byte): GitProtocolObjectType {
            return entries.first { it.value == type }
        }
    }
}
