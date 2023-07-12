package hu.simplexion.z2.schema

interface SchemaField<T> {

    val name: String
    val type: SchemaFieldType
    val nullable: Boolean
    val default: T

    fun toTypedValue(anyValue: Any?, fails: MutableList<ValidationFailInfo>): T?

    fun validate(parent: String? = null, anyValue: Any?): FieldValidationResult {
        val fails = mutableListOf<ValidationFailInfo>()
        val value = toTypedValue(anyValue,fails)

        validateNullable(value, fails)

        return FieldValidationResult(
            name,
            fails.isEmpty(),
            fails
        )
    }

    fun validateNullable(value: T?, fails: MutableList<ValidationFailInfo>) {
        when {
            value != null -> validateNotNullable(value, fails)
            !nullable -> fails += ValidationFailInfoNull
        }
    }

    fun validateNotNullable(value: T, fails: MutableList<ValidationFailInfo>)

}
