package kt.grpc.parser

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kt.grpc.parser.lexer.Proto3Lexer
import org.junit.jupiter.api.Test

class ParserTest {

    private val serializerModule = SerializersModule {
        polymorphic(Node::class) {
            subclass(PublicImport::class)
            subclass(NormalImport::class)
            subclass(WeakImport::class)
            subclass(EnumNode::class)
            subclass(Message::class)
            subclass(Option::class)
            subclass(RepeatedField::class)
            subclass(OneOfField::class)
            subclass(MapField::class)
            subclass(NormalField::class)
            subclass(Service::class)
            subclass(Rpc::class)
        }
    }
    private val json = Json { prettyPrint = true; prettyPrintIndent = "  "; serializersModule = serializerModule }

    @Test
    fun test_syntax_proto3() {
        test("/syntax_proto3")
    }

    @Test
    fun test_proto3_import() {
        test("/import_proto3")
    }

    @Test
    fun test_proto3_enum() {
        test("/enum_proto3")
    }

    @Test
    fun test_proto3_message_1(){
        test("/message_1")
    }

    @Test
    fun test_proto3_message_2(){
        test("/message_2")
    }

    @Test
    fun test_proto3_service(){
        test("/service_proto3")
    }

    private fun test(protoName: String) {
        val content = readResource("${protoName}.proto")
        val golden = readResource("${protoName}.ast.json").trim()
        val doc = Proto3Parser.parse(content)
        val serialized = json.encodeToString(doc)
        assert(serialized == golden)
    }
}
