package hu.simplexion.z2.schematic.runtime.schema

import hu.simplexion.z2.schematic.runtime.Schematic
import hu.simplexion.z2.schematic.runtime.schema.validation.FieldValidationResult
import hu.simplexion.z2.schematic.runtime.schema.validation.SchematicValidationResult

class Schema(
    vararg val fields : SchemaField<*>
) {

    /**
     * Get a field by its name.
     */
    fun getField(fieldName : String) = fields.first { it.name == fieldName }

    /**
     * Calls the `validate` function of all fields in the schema on the value
     * that belongs to the field in [schematic].
     */
    fun validate(schematic : Schematic<*>) : SchematicValidationResult {
        var valid = true
        val fieldResults = mutableMapOf<String,FieldValidationResult>()

        for (field in fields) {
            val fieldResult = field.validate(schematic.schematicValues[field.name])
            valid = valid && fieldResult.valid
            fieldResults[field.name] = fieldResult
        }

        return SchematicValidationResult(
            valid,
            fieldResults
        )
    }

    /**
     * Calls the `validateSuspend` function of all fields in the schema on the value
     * that belongs to the field in [schematic].
     */
    suspend fun validateSuspend(schematic : Schematic<*>) : SchematicValidationResult{
        var valid = true
        val fieldResults = mutableMapOf<String,FieldValidationResult>()

        for (field in fields) {
            val fieldResult = field.validateSuspend(schematic.schematicValues[field.name])
            valid = valid && fieldResult.valid
            fieldResults[field.name] = fieldResult
        }

        return SchematicValidationResult(
            valid,
            fieldResults
        )
    }

    /**
     * Initializes all fields to the default value. These changes do **NOT**
     * go into the change list, nor they generate listener calls. Requires
     * `schematicValues` to be empty/
     *
     * @throws  IllegalStateException  `schematicValues` is not empty
     */
    fun initWithDefaults(schematic: Schematic<*>) {
        check(schematic.schematicValues.isEmpty()) { "initWithDefaults called on a non-empty schematic" }
        for (field in fields) {
            field.initWithDefault(schematic)
        }
    }

}