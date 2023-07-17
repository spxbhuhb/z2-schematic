package foo.bar

import hu.simplexion.z2.schematic.runtime.Schematic

class Test : Schematic<Test> {
    val intField by int()
    val intFieldWithDefault by int(default = 5)
}

fun box(): String {
    val test = Test()

    if (test.intField != 0) "Fail"
    if (test.intFieldWithDefault != 5) "Fail"

    return "OK"
}