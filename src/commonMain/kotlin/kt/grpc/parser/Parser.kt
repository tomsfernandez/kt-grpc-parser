package kt.grpc.parser

import kt.grpc.parser.lexer.Token
import kt.grpc.parser.lexer.TokenType

interface ErrorHandler {
    fun handle(message: String, from: Int, to: Int)
}

interface TokenIterator {
    fun consumeOptional(type: TokenType): Content<String>?
    fun consumeOptional(type: TokenType, vararg values: String): Content<String>?
    fun <T> consumeOptional(type: TokenType, read: Read<T>, vararg values: T): Content<T>?
    fun consumeMany(type: TokenType): Content<String>
    fun consumeAny(vararg type: TokenType): Content<String>
    fun consumeAny(type: TokenType, vararg value: String): Content<String>
    fun <T> consumeAny(type: TokenType, read: Read<T>, vararg value: T): Content<T>
    fun consume(type: TokenType, value: String): Content<String>
    fun <T> consume(type: TokenType, read: Read<T>, value: T): Content<T>
    fun <T> consumeOptional(type: TokenType, read: Read<T>): Content<T>?
    fun <T> consume(type: TokenType, read: Read<T>): Content<T>
    fun consume(type: TokenType): Content<String>
    fun <T> content(token: Token, read: Read<T>): Content<T>

    fun peekAny(type: TokenType, vararg value: String): Content<String>?
    fun peek(type: TokenType, value: String): Content<String>?
    fun peek(type: TokenType): Content<String>?
    fun peek(): Token

    fun isEmpty(): Boolean
}

interface Parser {
    fun canParse(iterator: TokenIterator): Boolean
    fun parse(iterator: TokenIterator): Node
}

