package hu.simplexion.z2.schematic.runtime.schema.field

import hu.simplexion.z2.schematic.runtime.schema.SchemaField
import hu.simplexion.z2.schematic.runtime.schema.SchemaFieldType
import hu.simplexion.z2.schematic.runtime.schema.validation.ValidationFailInfo
import hu.simplexion.z2.schematic.runtime.schema.validation.fail
import hu.simplexion.z2.schematic.runtime.schema.validation.validationStrings

class BooleanSchemaField(
    override val name: String,
    override val nullable: Boolean = false,
    override val default: Boolean = false
) : SchemaField<Boolean> {

    override val type: SchemaFieldType
        get() = SchemaFieldType.Boolean

    override fun toTypedValue(anyValue: Any?, fails: MutableList<ValidationFailInfo>): Boolean? {
        if (anyValue == null) return null

        return when (anyValue) {
            is Boolean -> anyValue
            is String -> anyValue.toBooleanStrict()
            else -> {
                fails += fail(validationStrings.booleanFail)
                null
            }
        }
    }

    override fun validateNotNullable(value: Boolean, fails: MutableList<ValidationFailInfo>) {

    }
}