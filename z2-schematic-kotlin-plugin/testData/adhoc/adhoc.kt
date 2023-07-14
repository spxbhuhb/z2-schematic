package foo.bar

import hu.simplexion.z2.schematic.runtime.Schematic

class Adhoc : Schematic<Adhoc> {
    val intField by int(min = 5)
}
