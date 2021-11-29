package kt.grpc.parser

import kt.grpc.parser.lexer.TokenType

class ImportParser : Parser {
    override fun canParse(iterator: TokenIterator): Boolean {
        return iterator.peek(TokenType.IDENT, "import") != null
    }

    override fun parse(iterator: TokenIterator): Import {
        iterator.consume(TokenType.IDENT, "import")
        val importType = iterator.consumeOptional(TokenType.IDENT, "weak", "public")
        val path = iterator.consume(TokenType.STR_LIT)
        iterator.consume(TokenType.END_STATEMENT)
        return when(importType?.value) {
            "weak" -> WeakImport(StringScalar(path.value))
            "public" -> PublicImport(StringScalar(path.value))
            else -> NormalImport(StringScalar(path.value))
        }
    }
}
