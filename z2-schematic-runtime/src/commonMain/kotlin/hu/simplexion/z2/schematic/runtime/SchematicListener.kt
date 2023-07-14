package hu.simplexion.z2.schematic.runtime

class SchematicListener(
    val key: Any? = null,
    val func: (path: List<String>, change: SchematicChange) -> Unit
)