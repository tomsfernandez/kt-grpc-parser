package kt.grpc.parser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kt.grpc.parser.lexer.Token

sealed interface Node

@Serializable
sealed class Scalar<T> {

    abstract val value: T
}

@Serializable
data class StringScalar(override val value: String): Scalar<String>()

@Serializable
data class IntScalar(override val value: Int): Scalar<Int>()

@Serializable
data class Package(val value: StringScalar): Node

@Serializable
data class Syntax(val version: StringScalar): Node
@Serializable
data class Reserved(val ranges: List<Range>, val fields: List<String>, val fieldNumbers: List<String>): Node
@Serializable
data class Range(val from: Int, val to: Int)

sealed interface Import: Node {
    val path: StringScalar
}
@Serializable
@SerialName("Weak")
data class WeakImport(override val path: StringScalar): Import
@Serializable
@SerialName("Public")
data class PublicImport(override val path: StringScalar): Import
@Serializable
@SerialName("Normal")
data class NormalImport(override val path: StringScalar): Import
@Serializable
data class Option(val name: StringScalar, val value: StringScalar): Node

sealed interface Field: Node {
    val name: StringScalar
}

@Serializable
data class RepeatedField(val fieldType: StringScalar, val number: IntScalar, override val name: StringScalar, val options: List<Option>): Field

@Serializable
data class NormalField(val fieldType: StringScalar, val number: IntScalar, override val name: StringScalar, val options: List<Option>): Field

@Serializable
data class OneOfField(val oneOf: List<Field>, override val name: StringScalar, val options: List<Option>): Field

@Serializable
data class MapField(val keyType: StringScalar, val valueType: StringScalar, val number: IntScalar, override val name: StringScalar, val options: List<Option>): Field

data class Content<T>(val value: T, val token: Token)

@Serializable
data class Document(val syntax: Syntax, val nodes: List<Node>)

@Serializable
data class EnumField(val options: List<Option>, val value: Int)

@Serializable
data class EnumNode(val name: StringScalar, val options: List<Option>, val fields: List<EnumField>): Node

@Serializable
data class Message(val name: StringScalar, val nodes: List<Node>): Node

@Serializable
data class Service(val name: StringScalar, val nodes: List<Node>): Node

@Serializable
data class Rpc(val name: StringScalar, val request: RpcComponent, val response: RpcComponent, val options: List<Option>): Node

@Serializable
data class RpcComponent(val isStream: Boolean, val requestType: StringScalar)
