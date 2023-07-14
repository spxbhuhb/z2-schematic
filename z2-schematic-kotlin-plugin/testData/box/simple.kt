package foo.bar

import hu.simplexion.z2.schematic.runtime.Schematic

class Test : Schematic<Adhoc> {
    val intField by int(min = 5)
}

fun box(): String {
    val test = Test()
    return if (test.intField == 0) "OK" else "Fail"
}