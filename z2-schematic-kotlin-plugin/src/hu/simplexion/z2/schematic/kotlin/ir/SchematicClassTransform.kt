/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addElement
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name

class SchematicClassTransform(
    override val pluginContext: SchematicPluginContext
) : IrElementTransformerVoidWithContext(), IrBuilder {

    lateinit var transformedClass: IrClass
    lateinit var schematicValuesGetter: IrFunctionSymbol
    lateinit var companionClass: IrClass
    lateinit var companionSchematicSchemaGetter: IrFunctionSymbol
    lateinit var schemaFieldsArg: IrVarargImpl

    override fun visitClassNew(declaration: IrClass): IrStatement {

        if (!declaration.superTypes.contains(pluginContext.schematicClass.typeWith(declaration.defaultType))) {
            return declaration
        }

        transformedClass = declaration
        schematicValuesGetter = checkNotNull(declaration.getPropertyGetter(SCHEMATIC_VALUES_PROPERTY)) { "missing $SCHEMATIC_VALUES_PROPERTY getter " }

        companionClass = declaration.addCompanionIfMissing()
        companionClass.addIrProperty(
            Name.identifier(SCHEMATIC_SCHEMA_PROPERTY),
            pluginContext.schemaClass.defaultType,
            inIsVar = false,
            buildSchemaInitializer()
        ).also {
            it.modality = Modality.FINAL
            companionSchematicSchemaGetter = it.getter!!.symbol
        }

        super.visitClassNew(declaration)

        return declaration
    }

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

        if (declaration.name.identifier == SCHEMATIC_SCHEMA_PROPERTY) {
            return declaration.accept(SchematicSchemaPropertyTransform(pluginContext, this), null) as IrStatement
        }

        if (!declaration.isDelegated) {
            return declaration
        }

        val fieldVisitor = SchematicFieldVisitor(pluginContext, this)
        declaration.accept(fieldVisitor, null)
        schemaFieldsArg.addElement(fieldVisitor.schemaField)

        return declaration.accept(SchematicPropertyTransform(pluginContext, this, fieldVisitor), null) as IrStatement
    }


}
