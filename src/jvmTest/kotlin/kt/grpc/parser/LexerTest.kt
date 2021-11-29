package kt.grpc.parser

import kt.grpc.parser.lexer.Proto3Lexer
import kt.grpc.parser.lexer.Token
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import kotlin.streams.toList

fun readResource(path: String): String {
    val stream =  LexerTest::class.java.getResourceAsStream(path) ?: throw FileNotFoundException("something")
    return stream.bufferedReader().lines().toList().joinToString("\n")
}

class LexerTest {

    @Test
    fun test_message_1() {
        test("/message_1")
    }

    @Test
    fun test_import_proto3() {
        test("/import_proto3")
    }

    @Test
    fun test_proto3_syntax() {
        test("/syntax_proto3")
    }

    @Test
    fun test_proto3_enum() {
        test("/enum_proto3")
    }

    @Test
    fun test_proto3_message_1() {
        test("/message_1")
    }

    @Test
    fun test_proto3_message_2() {
        test("/message_2")
    }

    @Test
    fun test_proto3_service(){
        test("/service_proto3")
    }

    private fun test(protoName: String) {
        val content = readResource("${protoName}.proto")
        val golden = readResource("${protoName}.tokens").trim()
        val tokens = Proto3Lexer.lex(content)
        val serialized = serialize(tokens).trim()
        assert(serialized == golden)
    }

    private fun serialize(tokens: List<Token>): String {
        return tokens.joinToString("\n") { it.toString() }
    }
}
