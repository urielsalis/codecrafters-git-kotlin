package com.urielsalis.codecrafters.git.domain

sealed class GitPacketLine

data object FlushPacketLine : GitPacketLine()

data class DataPacketLine(val data: String) : GitPacketLine()
