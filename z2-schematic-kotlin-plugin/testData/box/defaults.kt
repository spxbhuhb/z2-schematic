package foo.bar

import hu.simplexion.z2.schematic.runtime.Schematic

class Test : Schematic<Test>() {
    val boolean by boolean() // false
    val booleanWithDefault by boolean(default = true) // true
    val booleanNullDefault by boolean().nullable() // null
    val booleanNullWithDefaultFalse by boolean(default = false).nullable() // false
    val booleanNullWithDefaultTrue by boolean(default = true).nullable() // true

    val int by int()
    val intWithDefault by int(default = 5)

    val string by string()
    val stringDefault by string(default = "abc")
}

fun box(): String {
    val test = Test()

    if (test.boolean) return "Fail"
    if (!test.booleanWithDefault) return "Fail"
    if (test.booleanNullDefault != null) return "Fail"
    if (test.booleanNullWithDefaultFalse != false) return "Fail"
    if (test.booleanNullWithDefaultTrue != true) return "Fail"

    if (test.int != 0) return "Fail"
    if (test.intWithDefault != 5) return "Fail"

    if (test.string != "") return "Fail"
    if (test.stringDefault != "abc") return "Fail"

    return "OK"
}