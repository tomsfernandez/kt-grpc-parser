package kt.grpc.parser

import kt.grpc.parser.lexer.TokenType.*

class OptionParser: Parser {

    override fun canParse(iterator: TokenIterator): Boolean {
        return iterator.peek(IDENT, "option") != null
    }

    override fun parse(iterator: TokenIterator): Option {
        iterator.consume(IDENT, "option")
        val result = parseOptionContent(iterator)
        iterator.consume(END_STATEMENT)
        return result
    }

    fun parseOptionContent(iterator: TokenIterator): Option {
        val name = optionName(iterator)
        iterator.consume(EQUALS)
        val value = iterator.consumeAny(STR_LIT,
            BOOL_LIT,
            CONSTANT,
            FULL_IDENT).value
        return Option(StringScalar(name.value), StringScalar(value.toString()))
    }


    private fun optionName(iterator: TokenIterator): StringScalar {
        val firstName = iterator.consumeOptional(IDENT)
        val firstPartOfName = firstName?.value ?: groupedIdentifier(iterator)
        var rest = ""
        while(iterator.peek(DOT) != null) {
            val dotToken = iterator.consume(DOT)
            val identToken = iterator.consume(IDENT)
            rest += dotToken.value + identToken.value
        }
        return StringScalar(firstPartOfName + rest)
    }

    private fun groupedIdentifier(iterator: TokenIterator): String {
        iterator.consume(LEFT_PAREN)
        val name = iterator.consumeAny(FULL_IDENT, IDENT).value
        iterator.consume(RIGHT_PAREN)
        return "(${name})"
    }
}
