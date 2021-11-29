package kt.grpc.parser

import kt.grpc.parser.lexer.TokenType.*

class EnumParser: Parser {

    override fun canParse(iterator: TokenIterator): Boolean {
        return iterator.peek(IDENT, "enum") != null
    }

    override fun parse(iterator: TokenIterator): Node {
        iterator.consume(IDENT, "enum")
        val name = iterator.consume(IDENT).value
        return enumBody(iterator, name)
    }

    private fun enumBody(iterator: TokenIterator, name: String): EnumNode {
        iterator.consume(LEFT_BRACE)
        val optionsList = mutableListOf<Option>()
        val fieldsList = mutableListOf<EnumField>()
        var next = iterator.peek()
        var shouldBreak = false
        while(!shouldBreak) {
            when {
                next.type == END_STATEMENT -> {
                    shouldBreak = false
                    iterator.consume(END_STATEMENT)
                    next = iterator.peek()
                }
                OptionParser().canParse(iterator) -> {
                    optionsList.add(OptionParser().parse(iterator))
                    shouldBreak = false
                    next = iterator.peek()
                }
                peekField(iterator) != null -> {
                    fieldsList.add(enumField(iterator))
                    shouldBreak = false
                    next = iterator.peek()
                }
                else -> {
                    shouldBreak = true
                }
            }
        }
        iterator.consume(RIGHT_BRACE)
        return EnumNode(StringScalar(name), optionsList, fieldsList)
    }

    private fun peekField(iterator: TokenIterator): Content<String>? {
        return iterator.peek(IDENT)
    }

    private fun enumField(iterator: TokenIterator): EnumField {
        iterator.consume(IDENT)
        iterator.consume(EQUALS)
        val sign = if(iterator.consumeOptional(DASH, "-") != null) -1 else 1
        val valueTokenContent = iterator.consume(INT_LIT, IntRead)
        val value = valueTokenContent.value * sign
        val shouldParseOption = iterator.consumeOptional(LEFT_BRACKET)?.value == "["
        val options = mutableListOf<Option>()
        if (shouldParseOption) {
            options.add(enumValueOption(iterator))
            while(iterator.consumeOptional(COMMA) != null) {
                options.add(enumValueOption(iterator))
            }
            iterator.consume(RIGHT_BRACKET)
        }
        return EnumField(options, value)
    }

    private fun enumValueOption(iterator: TokenIterator): Option {
        return OptionParser().parseOptionContent(iterator)
    }
}
