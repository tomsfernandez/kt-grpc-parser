package kt.grpc.parser

import kt.grpc.parser.lexer.TokenType

class ReservedParser: Parser {
    override fun canParse(iterator: TokenIterator): Boolean {
        return iterator.peek(TokenType.IDENT, "reserved") != null
    }

    override fun parse(iterator: TokenIterator): Reserved {
        iterator.consume(TokenType.IDENT, "reserved")
        val maybeIntToken = iterator.peek(TokenType.INT_LIT)
        return if (maybeIntToken != null) {
            parseReservedFieldNames(iterator)
        } else {
            parseReservedRanges(iterator)
        }
    }

    fun parseReservedFieldNames(iterator: TokenIterator): Reserved {
        val fields = mutableListOf<String>()
        val field = iterator.consume(TokenType.IDENT)
        fields.add(field.value)
        while(iterator.consumeOptional(TokenType.COMMA) != null) {
            fields.add(iterator.consume(TokenType.IDENT).value)
        }
        return Reserved(emptyList(), emptyList(), fields)
    }

    fun parseReservedRanges(iterator: TokenIterator): Reserved {
        val ranges = mutableListOf<Range>()
        ranges.add(parseRange(iterator))
        while(iterator.consumeOptional(TokenType.COMMA) != null) {
            ranges.add(parseRange(iterator))
        }
        return Reserved(ranges, emptyList(), emptyList())
    }

    private fun parseRange(iterator: TokenIterator): Range {
        val from = iterator.consume(TokenType.INT_LIT, IntRead).value
        val maybeUpper = iterator.consumeOptional(TokenType.IDENT, "to")
        return if (maybeUpper != null) {
            val upperBound = iterator.consumeOptional(TokenType.INT_LIT, IntRead)
            return if (upperBound == null) {
                iterator.consume(TokenType.IDENT, "max")
                Range(from, Int.MAX_VALUE)
            } else {
                Range(from, upperBound.value)
            }
        } else {
            Range(from, from)
        }
    }
}
