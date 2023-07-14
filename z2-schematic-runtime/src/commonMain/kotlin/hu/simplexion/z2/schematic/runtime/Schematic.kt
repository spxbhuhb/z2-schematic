package hu.simplexion.z2.schematic.runtime

import hu.simplexion.z2.schematic.runtime.schema.Schema
import hu.simplexion.z2.schematic.runtime.schema.field.IntSchemaField
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class Schematic<T : Schematic<T>> {

    val schematicValues = mutableMapOf<String,Any?>()
    val schematicChanges = mutableMapOf<String,SchematicChange>()
    val schematicListeners = mutableListOf<SchematicListener>()

    open val schematicSchema : Schema
        get() = throw IllegalStateException("Schematic.schematicSchema.get should, most probably the compiler plugin is missing.")

    fun schematicChange(field : String, change : SchematicChange) {

    }

    fun schematicChangeInt(field : String, value : Int) {
        schematicValues[field] = value
    }

    @Suppress("UNUSED_PARAMETER")
    @SchematicDelegate(IntSchemaField::class)
    fun int(
        default: Int = 0,
        min: Int? = null,
        max: Int? = null
    ) = FakeDelegateProvider<T,Int>()

    @Suppress("UNUSED_PARAMETER")
    fun string(
        default: String = "",
        minLength: Int? = null,
        maxLength: Int? = null,
        blank : Boolean? = null,
        pattern : String? = null
    ) = FakeDelegateProvider<T,String>()

    class FakeDelegate<T, V> : ReadWriteProperty<T, V> {
        override fun getValue(thisRef: T, property: KProperty<*>): V {
            throw IllegalStateException("Schematic.FakeDelegate.getValue should never be called, most probably the compiler plugin is missing.")
        }
        override fun setValue(thisRef: T, property: KProperty<*>, value : V) {
            throw IllegalStateException("Schematic.FakeDelegate.setValue should never be called, most probably the compiler plugin is missing.")
        }
    }

    class FakeDelegateProvider<T, V> {
        operator fun provideDelegate(thisRef: T, prop: KProperty<*>): ReadWriteProperty<T,V> {
            return FakeDelegate()
        }
    }
}