package foo.bar

import hu.simplexion.z2.commons.util.UUID
import hu.simplexion.z2.schematic.runtime.Schematic

class Test : Schematic<Test>() {

    var uuidField by uuid<Test>()

}

fun box(): String {
    val test = Test()

    return if (test.uuidField == UUID.nil<Test>()) "OK" else "Fail"
}