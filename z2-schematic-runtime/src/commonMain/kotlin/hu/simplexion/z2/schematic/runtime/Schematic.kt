package hu.simplexion.z2.schematic.runtime

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class Schematic<T : Schematic<T>> {

    val schematicValues = mutableMapOf<String,Any?>()
    val schematicChanges = mutableMapOf<String,SchematicChange>()
    val schematicListeners = mutableListOf<SchematicListener>()

    fun int(
        default: Int = 0,
        min: Int? = null,
        max: Int? = null
    ) = FakeDelegateProvider<T,Int>()

    fun string(
        default: String = "",
        minLength: Int? = null,
        maxLength: Int? = null,
        blank : Boolean? = null,
        pattern : String? = null
    ) = FakeDelegateProvider<T,String>()

    class FakeDelegate<T, V> : ReadWriteProperty<T, V> {
        override fun getValue(thisRef: T, property: KProperty<*>): V {
            throw IllegalStateException("This delegate should never be called, most probably the compiler plugin is missing.")
        }
        override fun setValue(thisRef: T, property: KProperty<*>, value : V) {
            throw IllegalStateException("This delegate should never be called, most probably the compiler plugin is missing.")
        }
    }

    class FakeDelegateProvider<T, V> {
        operator fun provideDelegate(thisRef: T, prop: KProperty<*>): ReadOnlyProperty<T,V> {
            return FakeDelegate()
        }
    }
}