package hu.simplexion.z2.schematic.test.schema

import hu.simplexion.z2.schematic.runtime.Schematic
import hu.simplexion.z2.schematic.runtime.SchematicCompanion

class A : Schematic<A>() {
    var i by int(min = 100)
    companion object : SchematicCompanion<A>
}
