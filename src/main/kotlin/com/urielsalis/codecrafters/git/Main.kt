package com.urielsalis.codecrafters.git

import com.urielsalis.codecrafters.git.command.CatFileCommand
import com.urielsalis.codecrafters.git.command.HashObjectCommand
import com.urielsalis.codecrafters.git.command.InitCommand
import com.urielsalis.codecrafters.git.command.LsTreeCommand
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(
    name = "git",
    mixinStandardHelpOptions = true,
    subcommands = [InitCommand::class, CatFileCommand::class, HashObjectCommand::class, LsTreeCommand::class],
)
class GitCommand

fun main(args: Array<String>) {
    CommandLine(GitCommand()).execute(*args)
}
