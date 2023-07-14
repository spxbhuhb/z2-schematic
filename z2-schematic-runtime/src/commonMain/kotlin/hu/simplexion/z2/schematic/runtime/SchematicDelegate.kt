package hu.simplexion.z2.schematic.runtime

import hu.simplexion.z2.schematic.runtime.schema.SchemaField
import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class SchematicDelegate(
    val schemaFieldClass : KClass<out SchemaField>
)
