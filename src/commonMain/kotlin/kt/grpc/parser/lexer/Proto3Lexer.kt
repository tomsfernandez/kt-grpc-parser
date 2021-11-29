package kt.grpc.parser.lexer

object Proto3Lexer {
    private val lexers = listOf(
        whitespace,
        block_comment,
        line_comment,
        dot,
        ineq_left,
        ineq_right,
        dash,
        comma,
        left_paren,
        right_paren,
        right_bracket,
        left_bracket,
        right_brace,
        left_brace,
        equals,
        boolLit,
        ident,
        fullIdent,
        messageType,
        enumType,
        intLit,
        decimalLit,
        octalLit,
        hexLit,
        decimals,
        exponent,
        floatLit,
        strLit,
        end
    )

    fun lex(string: String): List<Token> {
        var from = 0
        val buffer = mutableListOf<Token>()
        while(from < string.length) {
            val lexer = lexers.firstOrNull { it.lex(string, from) != null }
                ?: throw LexerException("Couldn't find lexer for ${string[from]} in position $from")
            val token = lexer.lex(string, from) ?: throw RuntimeException("asd")
            buffer.add(token)
            from = token.to
        }
        return buffer.toList()
    }
}
