package foo.bar

import hu.simplexion.z2.schematic.runtime.schema.Schema
import hu.simplexion.z2.schematic.runtime.schema.field.IntSchemaField
import hu.simplexion.z2.schematic.runtime.Schematic

class Test : Schematic<Test>() {

    var intField by int(min = 5)

}

fun box(): String {
    val test = Test()
    return "OK"
}