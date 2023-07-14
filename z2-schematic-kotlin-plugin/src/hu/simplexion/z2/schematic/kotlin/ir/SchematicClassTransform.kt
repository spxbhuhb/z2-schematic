/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.jvm.ir.receiverAndArgs
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.primaryConstructor

class SchematicClassTransform(
    override val pluginContext: SchematicPluginContext
) : IrElementTransformerVoidWithContext(), IrBuilder {

    lateinit var transformedClass: IrClass
    lateinit var schematicValuesGetter: IrFunctionSymbol
    lateinit var companionClass: IrClass
    lateinit var schemaFieldsArg: IrVarargImpl

    override fun visitClassNew(declaration: IrClass): IrStatement {

        if (!declaration.superTypes.contains(pluginContext.schematicClass.typeWith(declaration.defaultType))) {
            return declaration
        }

        transformedClass = declaration
        schematicValuesGetter = checkNotNull(declaration.getPropertyGetter(SCHEMATIC_VALUES_PROPERTY)) { "missing $SCHEMATIC_VALUES_PROPERTY getter " }

        super.visitClassNew(declaration)

        companionClass = declaration.addCompanionIfMissing()
//        companionClass.addIrProperty(
//            Name.identifier(SCHEMATIC_SCHEMA_PROPERTY),
//            pluginContext.schemaClass.defaultType,
//            inIsVar = false,
//            buildSchemaInitializer()
//        )

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
        if (!declaration.isDelegated) return declaration
        val backingField = declaration.backingField ?: return declaration

        // TODO checks and feedbacks in visitPropertyNew (non-schematic delegate?)

        // this is the call to provideDelegate
        // it's first argument is the call to the type function (`int`, `string` etc.)

        val provideDelegateCall = backingField.initializer?.expression ?: return declaration
        if (provideDelegateCall !is IrCall) return declaration

        // this is the call to the type function (`int`, `string`, etc)
        // it's parameters are the field constraints

        val delegateFunCall = provideDelegateCall.receiverAndArgs().first()
        if (delegateFunCall !is IrCall) return declaration

        val delegateFun = delegateFunCall.symbol.owner
        val delegateFunAnnotation = delegateFun.annotations.first { it.symbol == pluginContext.schematicDelegateConstructor.symbol }
        val fieldClassExpression = delegateFunAnnotation.getValueArgument(DELEGATE_ANNOTATION_FIELD_CLASS_INDEX)
        if (fieldClassExpression !is IrClassReference) return declaration

        addSchemaField(delegateFun, fieldClassExpression.classType)

        return declaration.accept(SchematicPropertyTransform(pluginContext, this), null) as IrStatement
    }

    fun addSchemaField(delegateFun: IrSimpleFunction, fieldClassType: IrType) {
        // TODO add element to the schemaFieldArgs
    }
}
