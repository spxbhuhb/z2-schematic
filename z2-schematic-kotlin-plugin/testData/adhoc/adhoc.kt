package foo.bar

import hu.simplexion.z2.schematic.runtime.schema.Schema
import hu.simplexion.z2.schematic.runtime.schema.field.IntSchemaField
import hu.simplexion.z2.schematic.runtime.Schematic

class Adhoc : Schematic<Adhoc>() {

    var intField by int(min = 5)

}

fun box(): String {
    val test = Adhoc()
    test.intField = 11
    if (test.schematicValues["intField"] != 11) return "Fail"
    return if (test.intField == 11) "OK" else "Fail"
}