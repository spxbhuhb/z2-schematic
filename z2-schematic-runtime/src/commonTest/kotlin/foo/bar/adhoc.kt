package foo.bar

import hu.simplexion.z2.schematic.runtime.Schematic
import hu.simplexion.z2.schematic.runtime.SchematicAccessFunction
import hu.simplexion.z2.schematic.runtime.context.SchematicAccessContext
import hu.simplexion.z2.schematic.runtime.schema.SchemaFieldType

class Test : Schematic<Test>() {
    var intField by int(min = 5).nullable()
}

@SchematicAccessFunction
fun testFun(context: SchematicAccessContext? = null, accessor: () -> Any?): SchemaFieldType {
    checkNotNull(context)
    return context.field.type
}

fun box(): String {
    val test = Test()
    val type = testFun { test.intField }
    return if (type == SchemaFieldType.Int) "OK" else "Fail: invalid return type (not int)"
}