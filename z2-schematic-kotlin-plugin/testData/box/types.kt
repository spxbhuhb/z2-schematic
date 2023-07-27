package foo.bar

import hu.simplexion.z2.schematic.runtime.schema.Schema
import hu.simplexion.z2.schematic.runtime.schema.field.IntSchemaField
import hu.simplexion.z2.schematic.runtime.Schematic

class Test : Schematic<Test>() {

    var booleanField by boolean()
    var durationField by duration()
    var instantField by instant()
    var intField by int()
    var localDateField by localDate()
    val localDateTimeField by localDateTime()
    val longField by long()
    val stringField by string()
    val uuidField by uuid<Test>()
    val schematicField by schematic<Test>().nullable()
}

fun box(): String {
    val test = Test()
    return "OK"
}