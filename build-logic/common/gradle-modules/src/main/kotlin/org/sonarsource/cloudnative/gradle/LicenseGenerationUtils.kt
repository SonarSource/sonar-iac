/*
 * SonarSource Cloud Native Gradle Modules
 * Copyright (C) 2024-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonarsource.cloudnative.gradle

import java.io.File
import java.io.IOException
import java.security.MessageDigest
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger

/**
 * Compares two directories recursively to check for equality.
 * Two directories are considered equal if they have the exact same
 * directory structure and all corresponding files have identical content.
 *
 * @param dir1 The first directory.
 * @param dir2 The second directory.
 * @return `true` if the directories are equal, `false` otherwise.
 */
fun areDirectoriesEqual(
    dir1: File,
    dir2: File,
    logger: Logger,
): Boolean {
    if (!dir1.isDirectory || !dir2.isDirectory) {
        logger.warn("One or both paths are not directories.")
        return false
    }
    logger.lifecycle("Comparing directories: ${dir1.name} and ${dir2.name}")

    try {
        // 1. Walk both directory trees and map files by their relative path
        val files1 = dir1.walk()
            .filter { it.isFile }
            .associateBy { it.relativeTo(dir1) }

        val files2 = dir2.walk()
            .filter { it.isFile }
            .associateBy { it.relativeTo(dir2) }

        // 2. Compare the directory structure (based on file paths)
        if (files1.keys != files2.keys) {
            logger.warn("Directory structures do not match.")
            logger.warn("Files only in ${dir1.name}: ${files1.keys - files2.keys}")
            logger.warn("Files only in ${dir2.name}: ${files2.keys - files1.keys}")
            return false
        }

        // 3. Compare the content of each matching file
        for (relativePath in files1.keys) {
            val file1 = files1[relativePath]!!
            val file2 = files2[relativePath]!!

            // Quick check: compare file sizes first
            if (file1.length() != file2.length()) {
                logger.warn("File size mismatch: $relativePath")
                return false
            }

            // Full check: compare byte content
            val checksum1 = getFileChecksum(file1)
            val checksum2 = getFileChecksum(file2)
            if (checksum1 != checksum2) {
                logger.warn("File content mismatch: $relativePath")
                return false
            }
        }

        // If all checks pass, the directories are equal
        return true
    } catch (e: IOException) {
        logger.error("An error occurred during comparison: ${e.message}")
        return false
    }
}

fun getFileChecksum(file: File): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digestBytes = md.digest(file.readBytes())

    // 4. Convert the byte array to a hexadecimal string
    // "%02x" formats each byte as two lowercase hex digits
    return digestBytes.joinToString("") { "%02x".format(it) }
}

fun copyDirectory(
    sourceDir: File,
    destinationDir: File,
    logger: Logger,
) {
    val errors = mutableListOf<String>()

    destinationDir.deleteRecursively()
    sourceDir.copyRecursively(
        target = destinationDir,
        overwrite = true,
        onError = { file, exception ->
            logger.warn("Failed to copy $file: ${exception.message}")
            errors.add(file.name)
            OnErrorAction.SKIP // Skip this file and continue
        }
    )

    if (errors.isEmpty()) {
        logger.lifecycle("Directory ${sourceDir.name} copied successfully to ${destinationDir.name}")
    } else {
        throw GradleException("Failed to copy ${errors.size} files.")
    }
}
