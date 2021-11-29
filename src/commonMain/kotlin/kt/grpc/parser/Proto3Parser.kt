package kt.grpc.parser

import kt.grpc.parser.lexer.Proto3Lexer
import kt.grpc.parser.lexer.Token

object Proto3Parser {

    private val parsers = listOf(
        PackageParser(),
        ImportParser(),
        OptionParser(),
        ReservedParser(),
        EnumParser(),
        MessageParser(),
        ServiceParser()
    )

    fun parse(content: String): Document {
        val tokens = Proto3Lexer.lex(content)
        return parse(content, tokens)
    }

    fun parse(content: String, tokens: List<Token>): Document {
        val iterator = DefaultTokenIterator(content, tokens)
        return DocumentParser(parsers).parse(iterator)
    }
}
