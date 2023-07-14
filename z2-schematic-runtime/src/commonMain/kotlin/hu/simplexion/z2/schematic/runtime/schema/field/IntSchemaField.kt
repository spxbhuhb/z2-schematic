package hu.simplexion.z2.schematic.runtime.schema.field

import hu.simplexion.z2.schematic.runtime.schema.SchemaField
import hu.simplexion.z2.schematic.runtime.schema.SchemaFieldType

class IntSchemaField(
    override val name: String,
    val min: Int?,
    val max: Int?,
) : SchemaField {

    override val type: SchemaFieldType
        get() = SchemaFieldType.Int

}