package kt.grpc.parser.lexer

fun Lexer.named(name: String): Lexer {
    return NamedLexer(name, this)
}

fun Lexer.or(lexer: Lexer): Lexer {
    return when(this) {
        is OrLexer -> this.copy(lexers = this.lexers + lexer)
        else -> OrLexer(listOf(this, lexer))
    }
}

fun Lexer.or(lexer: Lexer, type: TokenType): Lexer {
    return when(this) {
        is OrLexer -> this.copy(lexers = this.lexers + lexer, type)
        else -> OrLexer(listOf(this, lexer), type)
    }
}

fun Lexer.or(string: String, type: TokenType): Lexer {
    return when(this) {
        is OrLexer -> this.copy(lexers = this.lexers + RegexLexer(string, TokenType.ANONYMOUS), type)
        else -> OrLexer(listOf(this, RegexLexer(string, TokenType.ANONYMOUS)), type)
    }
}

fun Lexer.or(string: String): Lexer {
    return when(this) {
        is OrLexer -> this.copy(lexers = this.lexers + RegexLexer(string, TokenType.ANONYMOUS))
        else -> OrLexer(listOf(this, RegexLexer(string, TokenType.ANONYMOUS)))
    }
}

fun Lexer.and(lexer: Lexer, type: TokenType): Lexer {
    return when(this) {
        is AndLexer -> this.copy(lexers = this.lexers + lexer, type)
        else -> AndLexer(listOf(this, lexer), type)
    }
}

fun Lexer.and(lexer: Lexer): Lexer {
    return when(this) {
        is AndLexer -> this.copy(lexers = this.lexers + lexer, TokenType.ANONYMOUS)
        else -> AndLexer(listOf(this, lexer), TokenType.ANONYMOUS)
    }
}

fun Lexer.and(string: String): Lexer {
    return when(this) {
        is AndLexer -> this.copy(lexers = this.lexers + RegexLexer(string, TokenType.ANONYMOUS), TokenType.ANONYMOUS)
        else -> AndLexer(listOf(this, RegexLexer(string, TokenType.ANONYMOUS)), TokenType.ANONYMOUS)
    }
}

fun String.and(lexer: Lexer): Lexer {
    return when(lexer) {
        is AndLexer -> lexer.copy(lexers = listOf(RegexLexer(this, TokenType.ANONYMOUS)) + lexer.lexers,
            TokenType.ANONYMOUS)
        else -> AndLexer(listOf(RegexLexer(this, TokenType.ANONYMOUS), lexer), TokenType.ANONYMOUS)
    }
}

fun String.and(lexer: Lexer, type: TokenType): Lexer {
    return when(lexer) {
        is AndLexer -> lexer.copy(lexers = listOf(RegexLexer(this, TokenType.ANONYMOUS)) + lexer.lexers, type)
        else -> AndLexer(listOf(RegexLexer(this, TokenType.ANONYMOUS), lexer), type)
    }
}

fun String.and(string: String): Lexer {
    return AndLexer(listOf(RegexLexer(this, TokenType.ANONYMOUS), RegexLexer(string, TokenType.ANONYMOUS)),
        TokenType.ANONYMOUS)
}

fun String.or(lexer: Lexer, type: TokenType): Lexer {
    return when(lexer) {
        is OrLexer -> lexer.copy(lexers = listOf(RegexLexer(this, TokenType.ANONYMOUS)) + lexer.lexers, type)
        else -> OrLexer(listOf(RegexLexer(this, TokenType.ANONYMOUS), lexer), type)
    }
}

fun repeat(lexer: Lexer, type: TokenType): Lexer {
    return RepeatLexer(lexer, type)
}

fun repeat(lexer: Lexer): Lexer {
    return RepeatLexer(lexer, TokenType.ANONYMOUS)
}

fun repeat(lexer: Lexer, end: (Char) -> Boolean): Lexer {
    return RepeatLexer(lexer, TokenType.ANONYMOUS, end)
}

fun option(lexer: Lexer): Lexer {
    return OptionLexer(lexer)
}

fun option(string: String): Lexer {
    return RegexLexer(string, TokenType.ANONYMOUS)
}
