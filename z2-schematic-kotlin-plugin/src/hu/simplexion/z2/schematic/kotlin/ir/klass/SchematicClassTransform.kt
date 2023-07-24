/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir.klass

import hu.simplexion.z2.schematic.kotlin.ir.*
import hu.simplexion.z2.schematic.kotlin.ir.util.IrBuilder
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addElement
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

class SchematicClassTransform(
    override val pluginContext: SchematicPluginContext
) : IrElementTransformerVoidWithContext(), IrBuilder {

    lateinit var transformedClass: IrClass
    lateinit var schematicValuesGetter: IrFunctionSymbol
    lateinit var schematicChange: IrFunctionSymbol
    lateinit var companionClass: IrClass
    lateinit var companionSchematicSchemaGetter: IrFunctionSymbol

    lateinit var schemaFieldsArg: IrVarargImpl
    lateinit var initializer: IrAnonymousInitializer

    // index of the field in `Schema.fields`
    var fieldIndex = 0

    override fun visitClassNew(declaration: IrClass): IrStatement {

        if (::transformedClass.isInitialized) return declaration

        transformedClass = declaration
        schematicChange = findSchematicChange()
        schematicValuesGetter = checkNotNull(declaration.getPropertyGetter(SCHEMATIC_VALUES_PROPERTY)) { "missing $SCHEMATIC_VALUES_PROPERTY getter " }

        addOrGetCompanionClass()
        addInitializer()

        super.visitClassNew(declaration)

        return declaration
    }

    private fun addOrGetCompanionClass() {
        companionClass = transformedClass.addCompanionIfMissing()

        companionClass.addIrProperty(
            Name.identifier(SCHEMATIC_SCHEMA_PROPERTY),
            pluginContext.schemaClass.defaultType,
            inIsVar = false,
            buildSchemaInitializer(),
            listOf(pluginContext.schematicCompanionSchematicSchema)
        ).also {
            it.modality = Modality.FINAL
            companionSchematicSchemaGetter = it.getter!!.symbol
        }

        SchematicProtoCoders(pluginContext, this).build()

        if (companionClass.superTypes.firstOrNull { it.classFqName?.shortName()?.identifier == SCHEMATIC_COMPANION_CLASS } == null) {
            companionClass.superTypes += listOf(pluginContext.schematicCompanionClass.typeWith(transformedClass.defaultType))
        }
    }

    private fun addInitializer() {
        val initializer = irFactory.createAnonymousInitializer(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            origin = IrDeclarationOrigin.DEFINED,
            symbol = IrAnonymousInitializerSymbolImpl(),
            isStatic = false
        )

        initializer.parent = transformedClass
        initializer.body = DeclarationIrBuilder(irContext, initializer.symbol).irBlockBody {
            +irCall(
                pluginContext.schemaInitWithDefaults,
                dispatchReceiver = irCall(
                    companionSchematicSchemaGetter,
                    dispatchReceiver = irGetObject(companionClass.symbol)
                ),
                args = arrayOf(irGet(transformedClass.thisReceiver!!))
            )
        }

        transformedClass.declarations += initializer
    }

    private fun findSchematicChange(): IrFunctionSymbol =
        checkNotNull(
            transformedClass.functions.firstOrNull {
                it.name.identifier == SCHEMATIC_CHANGE &&
                    it.valueParameters.size == 3 &&
                    it.valueParameters[0].type == irBuiltIns.stringType &&
                    it.valueParameters[1].type == irBuiltIns.intType &&
                    it.valueParameters[2].type == irBuiltIns.anyNType
            }?.symbol
        ) { "missing schematicChange function" }

    fun buildSchemaInitializer(): IrExpression =

        IrConstructorCallImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            pluginContext.schemaClass.defaultType,
            pluginContext.schemaClass.owner.primaryConstructor!!.symbol,
            0, 0,
            1 // array of fields
        ).also { constructorCall ->
            constructorCall.putValueArgument(0, buildFragmentVarArg())
        }

    fun buildFragmentVarArg(): IrExpression {
        schemaFieldsArg = IrVarargImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            irBuiltIns.arrayClass.typeWith(pluginContext.schemaFieldType),
            pluginContext.schemaFieldType
        )
        return schemaFieldsArg
    }

    override fun visitPropertyNew(declaration: IrProperty): IrStatement {

        val name = declaration.name.identifier

        if (name == SCHEMATIC_SCHEMA_PROPERTY) {
            return declaration.accept(SchematicSchemaPropertyTransform(pluginContext, this), null) as IrStatement
        }

        if (name == SCHEMATIC_COMPANION_PROPERTY) {
            return declaration.accept(SchematicCompanionPropertyTransform(pluginContext, this), null) as IrStatement
        }

        if (!declaration.isDelegated) {
            return declaration
        }

        val fieldVisitor = SchematicFieldVisitor(pluginContext, this)
        declaration.accept(fieldVisitor, null)
        schemaFieldsArg.addElement(fieldVisitor.schemaField)

        return declaration.accept(SchematicPropertyTransform(pluginContext, this, fieldVisitor, fieldIndex++), null) as IrStatement
    }

}
