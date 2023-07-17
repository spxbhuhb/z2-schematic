package hu.simplexion.z2.schematic.kotlin.runners

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.streams.toList

fun runtimeClassPath() : List<File> =
    listOf(
        Files.list(Paths.get("../z2-schematic-runtime/build/libs/"))
            .filter { it.name.startsWith("z2-schematic-runtime-jvm-") }
            .toList()
            .maxBy { it.getLastModifiedTime() }
            .toFile()
    )