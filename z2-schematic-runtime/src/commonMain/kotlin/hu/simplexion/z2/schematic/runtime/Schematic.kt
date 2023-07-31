package hu.simplexion.z2.schematic.runtime

import hu.simplexion.z2.commons.util.UUID
import hu.simplexion.z2.schematic.runtime.access.SchematicAccessContext
import hu.simplexion.z2.schematic.runtime.schema.Schema
import hu.simplexion.z2.schematic.runtime.schema.field.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration

abstract class Schematic<T : Schematic<T>> {

    /**
     * The actual values stored in this schematic. Key is the name of the
     * field. Value may be missing if the field is nullable.
     */
    val schematicValues = mutableMapOf<String, Any?>()

    /**
     * The changes applied to this schematic since the last [schematicCollect].
     * Key is the name of the field.
     */
    var schematicChanges: MutableMap<String, SchematicChange>? = null

    /**
     * Listeners on this schematic, called after each change. The first
     * property of the pair is an optional key that can be used to
     * remove the listener.
     */
    var schematicListeners: MutableList<SchematicListenerEntry<T>>? = null

    class SchematicListenerEntry<ST>(
        val key: Any? = null,
        val func: SchematicListener<ST>
    )

    /**
     * Get the schema of this schematic. Returns with the value of
     * `schematicCompanion.schematicSchema`.
     */
    open val schematicSchema : Schema<T>
        get() = placeholder()

    /**
     * Get the companion object of this schematic.
     */
    open val schematicCompanion : SchematicCompanion<T>
        get() = placeholder()

    // -----------------------------------------------------------------------------------
    // Change management
    // -----------------------------------------------------------------------------------

    /**
     * Change the value of a field.
     *
     * - update the value in [schematicValues] (by calling `SchematicChange.patch`)
     * - adds the change to [schematicChanges]
     * - calls all listeners from [schematicListeners]
     */
    fun schematicChange(field: String, change: SchematicChange) {
        if (schematicChanges == null) schematicChanges = mutableMapOf()

        schematicChanges!![field] = change

        change.patch(schematicValues)

        schematicListeners?.let {
            for (listener in it) {
                @Suppress("UNCHECKED_CAST")
                listener.func(this as T, change)
            }
        }
    }

    /**
     * Creates a [SchematicChange] by calling `SchematicField.asChange` and then
     * calls [schematicChange] with it.
     */
    fun schematicChange(fieldName : String, fieldIndex : Int, value : Any?) {
        schematicChange(fieldName, schematicSchema.fields[fieldIndex].asChange(value))
    }

    /**
     * Get all the changes between the previous collect and this one.
     * Removes all the changes up until this collect.
     */
    fun schematicCollect() : Map<String,SchematicChange> {
        val c = schematicChanges ?: emptyMap()
        schematicChanges = null
        return c
    }

    /**
     * Apply all the changes. Calls [schematicChange] for each entry in
     * the map. This results calling the listeners for each change.
     */
    fun schematicPatch(changes: Map<String, SchematicChange>) {
        for (change in changes) {
            schematicChange(change.key, change.value)
        }
    }

    // -----------------------------------------------------------------------------------
    // Listeners
    // -----------------------------------------------------------------------------------

    /**
     * Adds a listener. This listeners cannot be removed later.
     */
    fun schematicAddListener(listener: SchematicListener<T>) {
        if (schematicListeners == null) schematicListeners = mutableListOf()
        schematicListeners!! += SchematicListenerEntry(null, listener)
    }

    /**
     * Adds a listener with a [key] that identifies the listener. The
     * listener mey be removed later with [schematicRemoveListener].
     */
    fun schematicAddListener(key: Any, listener: SchematicListener<T>) {
        if (schematicListeners == null) schematicListeners = mutableListOf()
        schematicListeners!! += SchematicListenerEntry(key, listener)
    }

    /**
     * Removes all listeners with the given [key].
     */
    fun schematicRemoveListener(key: Any) {
        if (schematicListeners == null) return
        schematicListeners!!.removeAll { it.key == key }
    }

    // -----------------------------------------------------------------------------------
    // Delegation
    //
    // These are used to trick the compiler and the IDE into the proper data types while
    // providing the schema information at the same time.
    //
    // Actually, the compiler plugin:
    //
    // - replaces all these delegations with:
    //   - a get from schematicValues (for the getter)
    //   - and call of schematicChange (for the setter)
    // - creates a schema field with the appropriate type and settings
    // - adds the schema field to the schema defined in the companion object
    // -----------------------------------------------------------------------------------

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(BooleanSchemaField::class)
    fun boolean(
        default: Boolean? = null
    ) = PlaceholderDelegateProvider<T,Boolean>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(DurationSchemaField::class)
    fun duration(
        default: Duration? = null
    ) = PlaceholderDelegateProvider<T,Duration>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(EmailSchemaField::class)
    fun email(
        blank: Boolean? = null
    ) = PlaceholderDelegateProvider<T,String>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(InstantSchemaField::class)
    fun instant(
        default: Instant? = null
    ) = PlaceholderDelegateProvider<T,Instant>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(IntSchemaField::class)
    fun int(
        default: Int? = null,
        min: Int? = null,
        max: Int? = null
    ) = PlaceholderDelegateProvider<T,Int>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(LocalDateSchemaField::class)
    fun localDate(
        default: LocalDate? = null
    ) = PlaceholderDelegateProvider<T, LocalDate>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(LocalDateTimeSchemaField::class)
    fun localDateTime(
        default: LocalDateTime? = null
    ) = PlaceholderDelegateProvider<T,LocalDateTime>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(LongSchemaField::class)
    fun long(
        default: Long? = null,
        min: Long? = null,
        max: Long? = null
    ) = PlaceholderDelegateProvider<T,Long>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(SchematicSchemaField::class)
    fun <V:Schematic<V>> schematic(
        default: V? = null
    ) = PlaceholderDelegateProvider<T,V>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(PhoneNumberSchemaField::class)
    fun phoneNumber(
        blank: Boolean? = null
    ) = PlaceholderDelegateProvider<T,String>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(StringSchemaField::class)
    fun secret(
        default: String? = null,
        minLength: Int? = null,
        maxLength: Int? = null,
        blank : Boolean? = null,
        pattern : Regex? = null
    ) = PlaceholderDelegateProvider<T,String>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(StringSchemaField::class)
    fun string(
        default: String? = null,
        minLength: Int? = null,
        maxLength: Int? = null,
        blank : Boolean? = null,
        pattern : Regex? = null
    ) = PlaceholderDelegateProvider<T,String>()

    @Suppress("UNUSED_PARAMETER")
    @FieldDefinitionFunction(UuidSchemaField::class)
    fun <V> uuid(
        default: UUID<V>? = null,
        nil : Boolean? = null,
    ) = PlaceholderDelegateProvider<T,UUID<V>>()

    @Suppress("UNCHECKED_CAST")
    @DefinitionTransformFunction
    fun <V> PlaceholderDelegateProvider<T,V>.nullable() : PlaceholderDelegateProvider<T,V?> {
        return this as PlaceholderDelegateProvider<T,V?>
    }

    class PlaceholderDelegate<T, V> : ReadWriteProperty<T, V> {
        override fun getValue(thisRef: T, property: KProperty<*>): V = placeholder()
        override fun setValue(thisRef: T, property: KProperty<*>, value : V) = placeholder()
    }

    class PlaceholderDelegateProvider<T, V> {
        operator fun provideDelegate(thisRef: T, prop: KProperty<*>): ReadWriteProperty<T,V> {
            return PlaceholderDelegate()
        }
    }

    // -----------------------------------------------------------------------------------
    // Accessor Support
    // -----------------------------------------------------------------------------------

    fun toSchematicAccessContext(fieldName : String) : SchematicAccessContext =
        SchematicAccessContext(this, schematicSchema.getField(fieldName), schematicValues[fieldName])

}