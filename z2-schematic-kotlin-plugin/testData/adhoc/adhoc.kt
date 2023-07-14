package foo.bar

import hu.simplexion.z2.schematic.runtime.schema.Schema
import hu.simplexion.z2.schematic.runtime.schema.field.IntSchemaField
import hu.simplexion.z2.schematic.runtime.Schematic

class Adhoc : Schematic<Adhoc>() {

    var intField : Int
        get() = schematicValues["intField"]!! as Int
        set(value) {
            schematicChangeInt("intField", value)
        }

}

//class Adhoc : Schematic<Adhoc> {
//
//    val intField : Int
//        get() = schematicValues["intField"]!! as Int
//        set(value) {
//            schematicChangeInt("intField", value)
//        }
//
//    override val schematicSchema
//        get() = Companion.schematicSchema
//
//    companion object {
//        val schematicSchema = Schema(
//            IntSchemaField("intField", 5, null)
//        )
//    }
//}