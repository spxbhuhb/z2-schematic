package hu.simplexion.z2.schematic.runtime.schema

import hu.simplexion.z2.schematic.runtime.SchematicChange
import hu.simplexion.z2.schematic.runtime.schema.validation.FieldValidationResult
import hu.simplexion.z2.schematic.runtime.schema.validation.ValidationFailInfo
import hu.simplexion.z2.schematic.runtime.schema.validation.ValidationFailInfoNull

interface SchemaField<T> {
    val name: String
    val type: SchemaFieldType
    val nullable: Boolean
    val default: T

    fun toTypedValue(anyValue: Any?, fails: MutableList<ValidationFailInfo>): T?

    fun validate(anyValue: Any?): FieldValidationResult {
        val fails = mutableListOf<ValidationFailInfo>()
        val value = toTypedValue(anyValue,fails)

        validateNullable(value, fails)

        return FieldValidationResult(
            name,
            fails.isEmpty(),
            fails
        )
    }

    suspend fun validateSuspend(anyValue: Any?) : FieldValidationResult {
        return validate(anyValue)
    }

    fun validateNullable(value: T?, fails: MutableList<ValidationFailInfo>) {
        when {
            value != null -> validateNotNullable(value, fails)
            !nullable -> fails += ValidationFailInfoNull
        }
    }

    fun validateNotNullable(value: T, fails: MutableList<ValidationFailInfo>)

    fun asChange(value : Any?) = SchematicChange(name, value)

}