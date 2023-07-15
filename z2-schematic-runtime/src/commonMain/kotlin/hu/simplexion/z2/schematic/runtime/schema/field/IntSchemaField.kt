package hu.simplexion.z2.schematic.runtime.schema.field

import hu.simplexion.z2.schematic.runtime.schema.SchemaField
import hu.simplexion.z2.schematic.runtime.schema.SchemaFieldType

class IntSchemaField(
    override val name: String,
    val default: Int = 0,
    val min: Int? = null,
    val max: Int? = null,
) : SchemaField {

    override val type: SchemaFieldType
        get() = SchemaFieldType.Int

}