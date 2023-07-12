package hu.simplexion.z2.schema

import hu.simplexion.z2.commons.util.UUID
import hu.simplexion.z2.schema.field.BooleanSchemaField
import hu.simplexion.z2.schema.field.IntegerSchemaField
import hu.simplexion.z2.schema.field.TextualSchemaField
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class Schema(
    val uuid: UUID<Schema>,
    //val locale: Locale
) {

    fun boolean(
        nullable: Boolean = false,
        default: Boolean = false,
    ): SchemaFieldDelegateProvider<Boolean, BooleanSchemaField> =

        SchemaFieldDelegateProvider { _, prop ->
            BooleanSchemaField(prop.name, nullable, default)
        }

    fun integer(
        nullable: Boolean = false,
        default: Int = 0,
        min: Int? = null,
        max: Int? = null
    ): SchemaFieldDelegateProvider<Int, IntegerSchemaField> =

        SchemaFieldDelegateProvider { _, prop ->
            IntegerSchemaField(prop.name, nullable, default, min, max)
        }

    fun textual(
        nullable: Boolean = false,
        default: String = "",
        minLength: Int? = null,
        maxLength: Int? = null,
        blank : Boolean? = null,
        pattern : String? = null
    ): SchemaFieldDelegateProvider<String, TextualSchemaField> =

        SchemaFieldDelegateProvider { _, prop ->
            TextualSchemaField(prop.name, nullable, default, minLength, maxLength, blank, pattern?.let { Regex(it)})
        }

    class SchemaFieldDelegate<DT, FT : SchemaField<DT>>(
        private val field: FT
    ) : ReadOnlyProperty<Schema, FT> {
        override fun getValue(thisRef: Schema, property: KProperty<*>): FT {
            return field
        }
    }

    class SchemaFieldDelegateProvider<DT, FT : SchemaField<DT>>(
        val builder: (thisRef: Schema, prop: KProperty<*>) -> FT
    ) {
        operator fun provideDelegate(thisRef: Schema, prop: KProperty<*>): ReadOnlyProperty<Schema, FT> {
            return SchemaFieldDelegate(builder(thisRef, prop))
        }
    }
}