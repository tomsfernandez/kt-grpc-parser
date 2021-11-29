package kt.grpc.parser

import kt.grpc.parser.lexer.TokenType.*

class FieldParser: Parser {


    override fun canParse(iterator: TokenIterator): Boolean {
        return iterator.peekAny(IDENT, "oneOf", "map") != null || peekType(iterator) != null
    }

    override fun parse(iterator: TokenIterator): Field {
        val fieldType = iterator.peek(IDENT)
        return when(fieldType?.value) {
            "oneOf" -> parseOneOf(iterator)
            "map" -> parseMap(iterator)
            else -> parseField(iterator)
        }
    }

    fun parseOneOf(iterator: TokenIterator): OneOfField {
        iterator.consume(IDENT, "oneof")
        val name = iterator.consume(IDENT)
        iterator.consume(LEFT_BRACE)
        val optionsList = mutableListOf<Option>()
        val fieldsList = mutableListOf<Field>()
        var next = iterator.peek()
        var shouldBreak = false
        while(!shouldBreak) {
            when {
                next.type == END_STATEMENT -> {
                    shouldBreak = false
                    next = iterator.peek()
                }
                OptionParser().canParse(iterator) -> {
                    optionsList.add(OptionParser().parse(iterator))
                    shouldBreak = false
                    next = iterator.peek()
                }
                peekType(iterator) != null -> {
                    fieldsList.add(parseNormal(iterator, false))
                    shouldBreak = false
                    next = iterator.peek()
                }
                else -> {
                    shouldBreak = true
                }
            }
        }
        iterator.consume(RIGHT_BRACE)
        return OneOfField(fieldsList, StringScalar(name.value), optionsList)
    }

    fun parseMap(iterator: TokenIterator): MapField {
        iterator.consume(IDENT, "map")
        iterator.consume(INEQ_LEFT)
        val keyType = parseKeyType(iterator)
        iterator.consume(COMMA)
        val valueType = parseType(iterator)
        iterator.consume(INEQ_RIGHT)
        val name = iterator.consume(IDENT)
        iterator.consume(EQUALS)
        val fieldNumber = iterator.consume(INT_LIT, IntRead)
        val options = parseOptions(iterator)
        return MapField(StringScalar(keyType),
            StringScalar(valueType),
            IntScalar(fieldNumber.value),
            StringScalar(name.value),
            options)
    }

    fun parseKeyType(iterator: TokenIterator): String {
        return iterator.consumeAny(IDENT, "int32", "int64", "uint32", "uint64", "sint32", "sint64", "fixed32", "fixed64", "sfixed32", "sfixed64", "bool", "string").value
    }

    fun parseField(iterator: TokenIterator): Field {
        val repeated = iterator.consumeOptional(IDENT, "repeated") != null
        return parseNormal(iterator, repeated)
    }

    fun parseNormal(iterator: TokenIterator, repeated: Boolean): Field {
        val type = parseType(iterator)
        val fieldName = iterator.consume(IDENT).value
        iterator.consume(EQUALS)
        val fieldNumber = iterator.consume(INT_LIT, IntRead)
        val options = parseOptions(iterator)
        return if (repeated) RepeatedField(StringScalar(type),
            IntScalar(fieldNumber.value),
            StringScalar(fieldName.toString()),
            options)
        else NormalField(StringScalar(type), IntScalar(fieldNumber.value), StringScalar(fieldName), options)
    }

    private fun parseOptions(iterator: TokenIterator): List<Option> {
        iterator.consumeOptional(LEFT_BRACKET) ?: return emptyList()
        val option = OptionParser().parseOptionContent(iterator)
        val optionList = mutableListOf(option)
        while(iterator.consumeOptional(COMMA) != null) {
            optionList.add(OptionParser().parseOptionContent(iterator))
        }
        iterator.consume(RIGHT_BRACKET)
        return optionList
    }

    private fun parseType(iterator: TokenIterator): String {
        return iterator.consumeOptional(IDENT, "double", "float", "int32", "int64", "uint32", "uint64", "sint32", "sint64", "fixed32", "fixed64", "sfixed32", "sfixed64", "bool", "string", "bytes")?.value ?:
            iterator.consumeOptional(MESSAGE_TYPE)?.value ?: iterator.consume(ENUM_TYPE).value
    }

    private fun peekType(iterator: TokenIterator): String? {
        return iterator.peekAny(IDENT, "double", "float", "int32", "int64", "uint32", "uint64", "sint32", "sint64", "fixed32", "fixed64", "sfixed32", "sfixed64", "bool", "string", "bytes")?.value ?:
        iterator.peek(MESSAGE_TYPE)?.value ?: iterator.peek(ENUM_TYPE)?.value
    }
}
