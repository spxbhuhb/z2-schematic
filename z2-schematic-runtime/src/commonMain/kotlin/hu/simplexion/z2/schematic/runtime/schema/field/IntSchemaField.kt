package hu.simplexion.z2.schematic.runtime.schema.field

import hu.simplexion.z2.schematic.runtime.schema.SchemaField
import hu.simplexion.z2.schematic.runtime.schema.SchemaFieldType
import hu.simplexion.z2.schematic.runtime.schema.validation.ValidationFailInfo
import hu.simplexion.z2.schematic.runtime.schema.validation.fail
import hu.simplexion.z2.schematic.runtime.schema.validation.validationStrings

class IntSchemaField(
    override val name: String,
    override val nullable: Boolean = false,
    override val definitionDefault: Int? = null,
    val min: Int? = null,
    val max: Int? = null,
) : SchemaField<Int> {

    override val type: SchemaFieldType
        get() = SchemaFieldType.Int

    override val naturalDefault = 0

    override fun toTypedValue(anyValue: Any?, fails: MutableList<ValidationFailInfo>): Int? {
        if (anyValue == null) return null

        return when (anyValue) {
            is Int -> anyValue
            is Number -> anyValue.toInt()
            is String -> anyValue.toIntOrNull()
            else -> {
                fails += fail(validationStrings.integerFail)
                null
            }
        }
    }

    override fun validateNotNullable(value: Int, fails: MutableList<ValidationFailInfo>) {
        if (min != null && value < min) fails += fail(validationStrings.minValueFail, min)
        if (max != null && value > max) fails += fail(validationStrings.maxValueFail, max)
    }
}