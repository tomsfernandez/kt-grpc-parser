package kt.grpc.parser

import kt.grpc.parser.lexer.Token
import kt.grpc.parser.lexer.TokenType
import kt.grpc.parser.lexer.TokenType.*

class ParserException(message: String): Exception(message)

class DefaultTokenIterator(private val content: String, private val tokens: List<Token>): TokenIterator {

    private var current: Int = 0
    private val ignored = setOf(WHITESPACE, BLOCK_COMMENT, LINE_COMMENT)

    override fun consumeOptional(type: TokenType): Content<String>? {
        val tokenContent = content(peek(), StringRead)
        return if (tokenContent.token.type == type) {
            current += 1
            tokenContent
        } else null
    }

    override fun consumeOptional(type: TokenType, vararg values: String): Content<String>? {
        return consumeOptional(type, StringRead, *values)
    }

    override fun <T> consumeOptional(type: TokenType, read: Read<T>, vararg values: T): Content<T>? {
        val tokenContent = content(peek(), read)
        return if (tokenContent.token.type == type && values.contains(tokenContent.value)) {
            current += 1
            tokenContent
        } else null
    }

    override fun consumeMany(type: TokenType): Content<String> {
        var buffer = mutableListOf<Content<String>>()
        var tokenContent = consume(type)
        buffer.add(tokenContent)
        while(tokenContent.token.type == type) {
            tokenContent = consume(type)
            buffer.add(tokenContent)
        }
        return Content(buffer.map { it.value }.joinToString { "\n" }, buffer.first().token)
    }

    override fun consumeAny(vararg type: TokenType): Content<String> {
        val token = peek()
        return if (type.contains(token.type)) {
            current += 1
            content(token, StringRead)
        } else throw ParserException("Expected $type, got ${token.type}")
    }

    override fun consumeAny(type: TokenType, vararg value: String): Content<String> {
        return consumeAny(type, StringRead, *value)
    }

    override fun <T> consumeAny(type: TokenType, read: Read<T>, vararg value: T): Content<T> {
        val tokenContent = content(peek(), read)
        return if (tokenContent.token.type == type && value.contains(tokenContent.value)) {
            current += 1
            tokenContent
        } else throw ParserException("Expected $type with value $value, got ${tokenContent.token.type} with value ${tokenContent.value}")
    }

    override fun consume(type: TokenType, value: String): Content<String> {
        return consume(type, StringRead, value)
    }

    override fun <T> consume(type: TokenType, read: Read<T>, value: T): Content<T> {
        val tokenContent = content(peek(), read)
        return if (tokenContent.token.type == type && value == tokenContent.value) {
            current += 1
            tokenContent
        } else throw ParserException("Expected $type with value '$value', got ${tokenContent.token.type} with value '${tokenContent.value}'")
    }

    override fun <T> consumeOptional(type: TokenType, read: Read<T>): Content<T>? {
        val token = peek()
        return if (token.type == type) {
            current += 1
            content(token, read)
        } else null
    }

    override fun consume(type: TokenType): Content<String> {
        return consume(type, StringRead)
    }

    override fun <T> consume(type: TokenType, read: Read<T>): Content<T> {
        val token = peek()
        return if (token.type == type) {
            current += 1
            content(token, read)
        } else throw ParserException("Expected ${type}, got ${token.type} / tokenNumber: $current")
    }

    override fun <T> content(token: Token, read: Read<T>): Content<T> {
        return Content(read.read(content, token.from, token.to), token)
    }

    override fun peekAny(type: TokenType, vararg value: String): Content<String>? {
        val tokenContent = content(peek(), StringRead)
        return if (tokenContent.token.type == type && value.contains(tokenContent.value)) tokenContent else null
    }

    override fun peek(type: TokenType, value: String): Content<String>? {
        val tokenContent = content(peek(), StringRead)
        return if (tokenContent.token.type == type && value == tokenContent.value) tokenContent else null
    }

    override fun peek(type: TokenType): Content<String>? {
        val token = peek()
        return if (token.type == type) content(token, StringRead) else null
    }

    override fun peek(): Token {
        while(ignored.contains(tokens[current].type) && !isEmpty()) {
            current += 1
        }
        if (isEmpty()) return Token(EOF, content.length, content.length)
        return tokens[current]
    }

    override fun isEmpty(): Boolean {
        return tokens.size == current
    }
}

interface Read<T> {
    fun read(content: String, from: Int, to: Int): T
}

object StringRead : Read<String> {
    override fun read(content: String, from: Int, to: Int): String {
        return content.substring(from, to)
    }
}

object IntRead : Read<Int> {
    override fun read(content: String, from: Int, to: Int): Int {
        return StringRead.read(content, from, to).toInt()
    }
}

object AnyRead : Read<Any> {
    override fun read(content: String, from: Int, to: Int): Any {
        return StringRead.read(content, from, to)
    }
}
