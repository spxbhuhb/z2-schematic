package hu.simplexion.z2.schematic.test.schema

import hu.simplexion.z2.schematic.runtime.Schematic
import hu.simplexion.z2.schematic.runtime.SchematicCompanion

enum class E {
    V1,
    V2
}

class A : Schematic<A>() {
    var i by int(min = 12)
    var e by enum()
    companion object : SchematicCompanion<A>
}
