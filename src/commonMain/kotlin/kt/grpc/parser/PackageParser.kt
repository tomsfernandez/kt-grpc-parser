package kt.grpc.parser

import kt.grpc.parser.lexer.TokenType

class PackageParser: Parser {
    override fun canParse(iterator: TokenIterator): Boolean {
        return iterator.peek(TokenType.IDENT, "package") != null
    }

    override fun parse(iterator: TokenIterator): Package {
        iterator.consume(TokenType.IDENT, "package")
        val fullPackage = iterator.consume(TokenType.FULL_IDENT)
        iterator.consume(TokenType.END_STATEMENT)
        return Package(StringScalar(fullPackage.value))
    }
}
