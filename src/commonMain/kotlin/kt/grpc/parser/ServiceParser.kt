package kt.grpc.parser

import kt.grpc.parser.lexer.TokenType.*

class ServiceParser: Parser {

    override fun canParse(iterator: TokenIterator): Boolean {
        return iterator.peek(IDENT, "service") != null
    }

    override fun parse(iterator: TokenIterator): Service {
        iterator.consume(IDENT, "service")
        val name = iterator.consume(IDENT)
        iterator.consume(LEFT_BRACE)
        val nodes = mutableListOf<Node>()
        while(iterator.peek(RIGHT_BRACE) == null) {
            when {
                OptionParser().canParse(iterator) -> nodes.add(OptionParser().parse(iterator))
                iterator.peek(IDENT, "rpc") != null -> nodes.add(parseRpc(iterator))
                else -> iterator.consumeOptional(END_STATEMENT)
            }
        }
        iterator.consume(RIGHT_BRACE)
        return Service(StringScalar(name.value), nodes)
    }

    private fun parseRpc(iterator: TokenIterator): Rpc {
        iterator.consume(IDENT, "rpc")
        val name = iterator.consume(IDENT)
        val request = parseComponent(iterator)
        iterator.consume(IDENT, "returns")
        val response = parseComponent(iterator)
        val isEnd = iterator.consumeOptional(END_STATEMENT) != null
        if (isEnd) return Rpc(StringScalar(name.value), request, response, emptyList())
        iterator.consume(LEFT_BRACE)
        val options = mutableListOf<Option>()
        while(iterator.peek(RIGHT_BRACE) == null) {
            when {
                OptionParser().canParse(iterator) -> options.add(OptionParser().parse(iterator))
                else -> iterator.consumeOptional(END_STATEMENT)
            }
        }
        iterator.consume(RIGHT_BRACE)
        return Rpc(StringScalar(name.value), request, response, options)
    }

    private fun parseComponent(iterator: TokenIterator): RpcComponent {
        val isStream = iterator.consumeOptional(IDENT, "stream") != null
        iterator.consume(LEFT_PAREN)
        val messageType = iterator.consumeAny(MESSAGE_TYPE, IDENT)
        iterator.consume(RIGHT_PAREN)
        return RpcComponent(isStream, StringScalar(messageType.value))
    }
}
