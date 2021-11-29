package testing

import kt.grpc.parser.LexerTest
import java.io.FileNotFoundException
import kotlin.streams.toList

actual fun readResource(path: String): String {
    val stream =  LexerTest::class.java.getResourceAsStream(path) ?: throw FileNotFoundException("$path not found in resources")
    return stream.bufferedReader().lines().toList().joinToString("\n")
}
