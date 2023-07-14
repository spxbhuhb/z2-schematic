/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.getSimpleFunction

/**
 * Replace the delegation with a getter and a setter.
 */
class SchematicPropertyTransform(
    override val pluginContext: SchematicPluginContext,
    val classTransform: SchematicClassTransform,
) : IrElementTransformerVoidWithContext(), IrBuilder {

    val transformedClass
        get() = classTransform.transformedClass

    lateinit var property: IrProperty
    lateinit var type: IrType

    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        property = declaration
        type = checkNotNull(property.getter) { "missing return type" }.returnType

        property.isDelegated = false
        property.backingField = null

        return super.visitPropertyNew(property)
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        check(declaration is IrSimpleFunction)
        val funName = declaration.name.asString()

        when {
            "get-" in funName -> transformGetter(declaration)
            "set-" in funName -> transformSetter(declaration)
            else -> throw IllegalStateException("unexpected property function: $funName")
        }

        return declaration
    }

    // TODO check getter and setter visibility and modality

    /**
     * ```text
     *   FUN name:<get-intField> visibility:public modality:FINAL <> ($this:foo.bar.Adhoc) returnType:kotlin.Int
     *     correspondingProperty: PROPERTY name:intField visibility:public modality:FINAL [var]
     *     $this: VALUE_PARAMETER name:<this> type:foo.bar.Adhoc
     *     BLOCK_BODY
     *       RETURN type=kotlin.Nothing from='public final fun <get-intField> (): kotlin.Int declared in foo.bar.Adhoc'
     *         TYPE_OP type=kotlin.Int origin=CAST typeOperand=kotlin.Int
     *           CALL 'public final fun CHECK_NOT_NULL <T0> (arg0: T0 of kotlin.internal.ir.CHECK_NOT_NULL?): {T0 of kotlin.internal.ir.CHECK_NOT_NULL & Any} declared in kotlin.internal.ir' type=kotlin.Any origin=EXCLEXCL
     *             <T0>: kotlin.Any
     *             arg0: CALL 'public abstract fun get (key: K of kotlin.collections.MutableMap): V of kotlin.collections.MutableMap? [fake_override,operator] declared in kotlin.collections.MutableMap' type=kotlin.Any? origin=null
     *               $this: CALL 'public final fun <get-schematicValues> (): kotlin.collections.MutableMap<kotlin.String, kotlin.Any?> [fake_override] declared in foo.bar.Adhoc' type=kotlin.collections.MutableMap<kotlin.String, kotlin.Any?> origin=GET_PROPERTY
     *                 $this: GET_VAR '<this>: foo.bar.Adhoc declared in foo.bar.Adhoc.<get-intField>' type=foo.bar.Adhoc origin=null
     *               key: CONST String type=kotlin.String value="intField"
     * ```
     */
    fun transformGetter(func: IrSimpleFunction) {
        func.origin = IrDeclarationOrigin.GENERATED_SETTER_GETTER

        func.body = DeclarationIrBuilder(irContext, func.symbol).irBlockBody {

            val getSchematicValues = irCall(
                classTransform.schematicValuesGetter,
                dispatchReceiver = irGet(func.dispatchReceiverParameter!!)
            )

            val getValue = irCall(
                pluginContext.mutableMapGet,
                dispatchReceiver = getSchematicValues,
                args = arrayOf(irConst(property.name.identifier))
            )

            val notNull = irCall(
                irBuiltIns.checkNotNullSymbol,
                args = arrayOf(getValue)
            ).apply {
                putTypeArgument(0, irBuiltIns.anyType)
            }

            +irReturn(
                irImplicitAs(type, notNull)
            )
        }
    }

    /**
     * ```text
     *   FUN name:<set-intField> visibility:public modality:FINAL <> ($this:foo.bar.Adhoc, value:kotlin.Int) returnType:kotlin.Unit
     *     correspondingProperty: PROPERTY name:intField visibility:public modality:FINAL [var]
     *     $this: VALUE_PARAMETER name:<this> type:foo.bar.Adhoc
     *     VALUE_PARAMETER name:value index:0 type:kotlin.Int
     *     BLOCK_BODY
     *       CALL 'public final fun schematicChangeInt (field: kotlin.String, value: kotlin.Int): kotlin.Unit [fake_override] declared in foo.bar.Adhoc' type=kotlin.Unit origin=null
     *         $this: GET_VAR '<this>: foo.bar.Adhoc declared in foo.bar.Adhoc.<set-intField>' type=foo.bar.Adhoc origin=null
     *         field: CONST String type=kotlin.String value="intField"
     *         value: GET_VAR 'value: kotlin.Int declared in foo.bar.Adhoc.<set-intField>' type=kotlin.Int origin=null
     * ```
     */
    fun transformSetter(func: IrSimpleFunction) {
        func.origin = IrDeclarationOrigin.GENERATED_SETTER_GETTER

        func.body = DeclarationIrBuilder(irContext, func.symbol).irBlockBody {

            +irCall(
                checkNotNull(transformedClass.getSimpleFunction(SCHEMATIC_CHANGE_INT)) { "missing $SCHEMATIC_CHANGE_INT" },
                dispatchReceiver = irGet(func.dispatchReceiverParameter!!)
            ).apply {
                putValueArgument(SCHEMA_CHANGE_FIELD_INDEX, irConst(property.name.identifier))
                putValueArgument(SCHEMA_CHANGE_VALUE_INDEX, irGet(func.valueParameters[0]))
            }
        }
    }


}
