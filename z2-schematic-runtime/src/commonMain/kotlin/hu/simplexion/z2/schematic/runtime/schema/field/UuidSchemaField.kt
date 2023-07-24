package hu.simplexion.z2.schematic.runtime.schema.field

import hu.simplexion.z2.commons.protobuf.ProtoMessage
import hu.simplexion.z2.commons.protobuf.ProtoMessageBuilder
import hu.simplexion.z2.commons.util.UUID
import hu.simplexion.z2.schematic.runtime.Schematic
import hu.simplexion.z2.schematic.runtime.schema.SchemaField
import hu.simplexion.z2.schematic.runtime.schema.SchemaFieldType
import hu.simplexion.z2.schematic.runtime.schema.validation.ValidationFailInfo
import hu.simplexion.z2.schematic.runtime.schema.validation.fail
import hu.simplexion.z2.schematic.runtime.schema.validation.validationStrings

class UuidSchemaField<T>(
    override val name: String,
    override val nullable: Boolean = false,
    override val definitionDefault: UUID<T>? = null,
    val nil : Boolean = false
) : SchemaField<UUID<T>> {

    override val type: SchemaFieldType
        get() = SchemaFieldType.String

    override val naturalDefault = UUID.nil<T>()

    override fun toTypedValue(anyValue: Any?, fails: MutableList<ValidationFailInfo>): UUID<T>? {
        if (anyValue == null) return null

        return when (anyValue) {
            is String -> UUID(anyValue)
            else -> {
                fails += fail(validationStrings.uuidFail)
                null
            }
        }
    }

    override fun validateNotNullable(value: UUID<T>, fails: MutableList<ValidationFailInfo>) {
        if (! nil && value == UUID.nil<T>()) fails += fail(validationStrings.nilFail)
    }

    override fun encodeProto(schematic: Schematic<*>, fieldNumber: Int, builder: ProtoMessageBuilder) {
        val value = toTypedValue(schematic.schematicValues[name], mutableListOf()) ?: return
        builder.uuid(fieldNumber, value)
    }

    override fun decodeProto(schematic: Schematic<*>, fieldNumber: Int, message: ProtoMessage) {
        val value = message.uuid<T>(fieldNumber)
        schematic.schematicValues[name] = value
    }

}