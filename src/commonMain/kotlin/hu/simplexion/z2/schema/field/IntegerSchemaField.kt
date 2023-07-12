package hu.simplexion.z2.schema.field

import hu.simplexion.z2.schema.*

class IntegerSchemaField(
    override val name: String,
    override val nullable: Boolean,
    override val default: Int,
    val min: Int?,
    val max: Int?,
) : SchemaField<Int> {

    override val type: SchemaFieldType
        get() = SchemaFieldType.Integer

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