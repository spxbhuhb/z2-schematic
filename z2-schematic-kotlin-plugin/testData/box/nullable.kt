package foo.bar

import hu.simplexion.z2.schematic.runtime.Schematic

class Test : Schematic<Test>() {
    var nullableField by int().nullable()
}

fun box(): String {
    val test = Test()

    test.nullableField = 5
    if (test.nullableField != 5) "Fail"

    test.nullableField = null
    if (test.nullableField != null) "Fail"

    return "OK"
}