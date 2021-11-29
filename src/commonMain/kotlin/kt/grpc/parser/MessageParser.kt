package kt.grpc.parser

import kt.grpc.parser.lexer.TokenType.*

class MessageParser: Parser {

    private val parsers: List<Parser> = listOf(
        EnumParser(),
        this,
        OptionParser(),
        FieldParser()
    )

    override fun canParse(iterator: TokenIterator): Boolean {
        return iterator.peek(IDENT, "message") != null
    }

    override fun parse(iterator: TokenIterator): Message {
        iterator.consume(IDENT, "message")
        val name = iterator.consume(IDENT).value
        iterator.consume(LEFT_BRACE)
        val nodes = mutableListOf<Node>()
        while(iterator.peek(RIGHT_BRACE) == null) {
            if (iterator.consumeOptional(END_STATEMENT) == null) {
                val parser = parsers.first { it.canParse(iterator) }
                nodes.add(parser.parse(iterator))
            }
        }
        iterator.consume(RIGHT_BRACE)
        return Message(StringScalar(name), nodes)
    }
}
