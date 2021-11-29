package kt.grpc.parser

import kt.grpc.parser.lexer.TokenType

class SyntaxParser : Parser {

    override fun canParse(iterator: TokenIterator): Boolean {
        return iterator.peek(TokenType.IDENT, "syntax") != null
    }

    override fun parse(iterator: TokenIterator): Syntax {
        iterator.consume(TokenType.IDENT, "syntax")
        iterator.consume(TokenType.EQUALS)
        iterator.consume(TokenType.STR_LIT, "\"proto3\"")
        iterator.consume(TokenType.END_STATEMENT)
        return Syntax(StringScalar("proto3"))
    }
}
