package hu.simplexion.z2.schema.field

import hu.simplexion.z2.schema.*


class BooleanSchemaField(
    override val name: String,
    override val nullable: Boolean,
    override val default: Boolean,
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