package kt.grpc.parser

class DocumentParser(val parsers: List<Parser>) {

    fun parse(iterator: TokenIterator): Document {
        val syntax = SyntaxParser().parse(iterator)
        val nodes = mutableListOf<Node>()
        while(!iterator.isEmpty()) {
            val parser = parsers.find { it.canParse(iterator) } !!
            val node = parser.parse(iterator)
            nodes.add(node)
        }
        return Document(syntax, nodes)
    }
}
