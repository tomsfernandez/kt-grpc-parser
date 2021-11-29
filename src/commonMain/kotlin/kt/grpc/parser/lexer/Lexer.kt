package kt.grpc.parser.lexer

import kt.grpc.parser.lexer.TokenType.*

class LexerException(message: String): Exception(message)

enum class TokenType {
    FULL_IDENT, IDENT, LETTER, DECIMAL_DIGIT, OCTAL_DIGIT, HEX_DIGIT, BOOL_LIT, ANONYMOUS, INT_LIT, MESSAGE_TYPE, ENUM_TYPE, DECIMAL_LIT, OCTAL_LIT, HEX_LIT,
    DECIMALS, EXPONENT, FLOAT_LIT, STR_LIT, END_STATEMENT, CONSTANT, RIGHT_BRACE, LEFT_BRACE, WHITESPACE, EQUALS, QUOTE, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET, COMMA, DASH,
    INEQ_LEFT, INEQ_RIGHT, EOF, DOT, LINE_COMMENT, BLOCK_COMMENT
}

data class Token(val type: TokenType, val from: Int, val to: Int)

interface Lexer {
    fun lex(source: String, from: Int, ignoreErrors: Boolean = false): Token?
}

val block_comment = RegexLexer("^\\/\\*\\*[^\\*\\/]\\*\\/", BLOCK_COMMENT)
val line_comment = RegexLexer("^\\/[^\n]*", LINE_COMMENT)
val dot = RegexLexer("^\\.", DOT)
val dash = RegexLexer("^-", DASH)
val ineq_left = RegexLexer("^<", INEQ_LEFT)
val ineq_right = RegexLexer("^>", INEQ_RIGHT)
val comma = RegexLexer("^,", COMMA)
val left_bracket = RegexLexer("^\\[", LEFT_BRACKET)
val right_bracket = RegexLexer("^\\]", RIGHT_BRACKET)
val left_paren = RegexLexer("^\\(", LEFT_PAREN)
val right_paren = RegexLexer("^\\)", RIGHT_PAREN)
val right_brace = RegexLexer("^\\}", RIGHT_BRACE)
val left_brace = RegexLexer("^\\{", LEFT_BRACE)
val whitespace = RegexLexer("^\\n|^ |^\\t|^\\r", WHITESPACE)
val equals = RegexLexer("^=", EQUALS)
val letter = RegexLexer("^[a-zA-Z]", LETTER)
val decimalDigit = RegexLexer("^[0-9]", DECIMAL_DIGIT)
val octalDigit = RegexLexer("^[0-9]", OCTAL_DIGIT)
val hexDigit = RegexLexer("^[a-fA-F0-9]", HEX_DIGIT)
val ident = letter.and(repeat(letter.or(decimalDigit).or("^_")), IDENT)
val messageType = option("^\\.").and(ident.and("^\\.")).and(ident, MESSAGE_TYPE)
val enumType = option("^\\.").and(ident.and("^\\.")).and(ident, ENUM_TYPE)
val fullIdent = ident.and(repeat("^\\.".and(ident)), FULL_IDENT)
val intLit = decimalDigit.or(octalDigit).or(hexDigit, INT_LIT)
val decimalLit = "^[1-9]".and(repeat(decimalDigit), DECIMAL_LIT)
val octalLit = "^0".and(repeat(octalDigit), OCTAL_LIT)
val hexLit = "^0".and("^[xX]").and(hexDigit).and(repeat(hexDigit), HEX_LIT)
val decimals = decimalDigit.and(repeat(decimalDigit), DECIMALS)
val exponent = "^[eE]".and(option("^[+-]")).and(decimals, EXPONENT)
val floatLit = "^(inf|nan)".or(
    (decimals.and("^\\.").and(option(decimals).and(option(exponent)))).or(decimals.and(exponent))
        .or("^\\.".and(decimals).and(option(
            exponent))), FLOAT_LIT
)
val boolLit = RegexLexer("^true", BOOL_LIT).or("^false", BOOL_LIT).named("BoolLit")
val quote = RegexLexer("^['|\"]", QUOTE)
val charEscape = RegexLexer("^\\[Abfnrtv\\'\"]", ANONYMOUS)
val octEscape = "^\\\\".and(octalDigit).and(octalDigit).and(octalDigit)
val hexExcape = "^\\\\[xX]".and(hexDigit).and(hexDigit)
val charValue = hexExcape.or(octEscape).or(charEscape).or("""^[^\\0\n\\]""")
val strLit = ("^'".and(repeat(charValue) { it == '\'' }).and("^'"))
    .or("^\"".and(repeat(charValue) { it == '"'}).and("^\""), STR_LIT).named("StrLit")
val end = RegexLexer("^;", END_STATEMENT)
val constant = fullIdent.or("^[-+]".and(intLit)).or("^-+".and(floatLit)).or(strLit).or(boolLit, CONSTANT)

data class NamedLexer(private val name: String, private val lexer: Lexer): Lexer {
    override fun lex(source: String, from: Int, ignoreErrors: Boolean): Token? {
        return lexer.lex(source, from, ignoreErrors)
    }
}

data class OrLexer(val lexers: List<Lexer>, private val type: TokenType = ANONYMOUS) : Lexer {
    override fun lex(source: String, from: Int, ignoreErrors: Boolean): Token? {
        return lexers.asSequence()
            .mapNotNull { it.lex(source, from) }
            .firstOrNull()?.copy(type = type)
    }
}

data class AndLexer(val lexers: List<Lexer>, private val type: TokenType) : Lexer {
    override fun lex(source: String, from: Int, ignoreErrors: Boolean): Token? {
        return try {
            val to = lexers.fold(from) { nextFrom, lexer ->
                val token = lexer.lex(source, nextFrom, ignoreErrors)
                if (token?.to == null && ignoreErrors) throw RuntimeException("something")
                if (token?.to == null && !ignoreErrors) throw IllegalArgumentException("something")
                token?.to !!
            }
            Token(type, from, to)
        } catch(e: IllegalArgumentException) {
            null
        }
    }
}

data class OptionLexer(private val lexer: Lexer) : Lexer {
    override fun lex(source: String, from: Int, ignoreErrors: Boolean): Token {
        return lexer.lex(source, from, ignoreErrors) ?: Token(ANONYMOUS, from, from)
    }
}

data class RepeatLexer(private val lexer: Lexer, private val type: TokenType, private val end: (Char) -> Boolean = { false }) :
    Lexer {
    override fun lex(source: String, from: Int, ignoreErrors: Boolean): Token {
        var nextFrom = lexer.lex(source, from, ignoreErrors)?.to
        var to = from
        while (nextFrom != null && !end(source[nextFrom])) {
            nextFrom = lexer.lex(source, nextFrom, ignoreErrors)?.to
            if (nextFrom != null) to = nextFrom
        }
        return Token(type, from, to)
    }
}

data class RegexLexer(private val regex: Regex, private val type: TokenType) : Lexer {

    constructor(regex: String, type: TokenType) : this(Regex(regex), type)

    override fun lex(source: String, from: Int, ignoreErrors: Boolean): Token? {
        val sourceSubset = source.substring(from)
        val match = regex.find(sourceSubset) ?: return null
        return Token(type, from + match.range.first, from + match.range.last + 1)
    }
}



