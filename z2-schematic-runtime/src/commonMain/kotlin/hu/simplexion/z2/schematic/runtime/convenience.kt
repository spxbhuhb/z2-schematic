package hu.simplexion.z2.schematic.runtime

import hu.simplexion.z2.commons.util.PublicApi

@PublicApi
fun Schematic<*>.dump() : String =
    schematicSchema.dump(this, "", mutableListOf()).joinToString("\n")