package com.bbrustol.core.infrastructure.network

import java.io.InputStreamReader
import java.util.*

class ResourceUtils {

    fun openFile(pathFile: String): String {

        val classLoader = this.javaClass.classLoader
        val resourceAsStream = classLoader?.getResourceAsStream(pathFile)

        val result = StringBuilder("")

        val scanner = Scanner(resourceAsStream)

        while (scanner.hasNextLine()) {
            result.append(scanner.nextLine())
        }

        return result.toString()
    }

    fun loadGraphQLQuery(path: String): String {
        val classLoader = Thread.currentThread().contextClassLoader
        return classLoader?.getResourceAsStream(path)?.use {
            InputStreamReader(it).readText()
        } ?: throw IllegalArgumentException("don't found GraphQL: $path")
    }
}

