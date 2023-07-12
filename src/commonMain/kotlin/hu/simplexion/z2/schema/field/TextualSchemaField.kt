package hu.simplexion.z2.schema.field

import hu.simplexion.z2.schema.*

class TextualSchemaField(
    override val name: String,
    override val nullable: Boolean,
    override val default: String,
    val minLength: Int?,
    val maxLength: Int?,
    val blank : Boolean?,
    val pattern : Regex?
) : SchemaField<String> {

    override val type: SchemaFieldType
        get() = SchemaFieldType.Textual

    override fun toTypedValue(anyValue: Any?, fails: MutableList<ValidationFailInfo>): String? {
        if (anyValue == null) return null

        return when (anyValue) {
            is String -> anyValue
            else -> anyValue.toString()
        }
    }

    override fun validateNotNullable(value: String, fails: MutableList<ValidationFailInfo>) {
        val length = value.length

        if (minLength != null && length < minLength) fails += fail(validationStrings.minLengthFail, minLength)
        if (maxLength != null && length > maxLength) fails += fail(validationStrings.maxValueFail, maxLength)
        if (blank == false && value.isBlank()) fails += fail(validationStrings.blankFail)
        if (pattern != null && !pattern.matches(value)) fails += fail(validationStrings.patternFail)
    }

}