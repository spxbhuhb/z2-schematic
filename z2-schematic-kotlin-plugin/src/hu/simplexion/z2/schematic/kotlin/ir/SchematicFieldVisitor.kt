/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import org.jetbrains.kotlin.backend.jvm.ir.receiverAndArgs
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

/**
 * Analyze the FDF call and the possible DEF calls.
 * Build the SchemaField constructor call.
 */
class SchematicFieldVisitor(
    override val pluginContext: SchematicPluginContext,
    val classTransform: SchematicClassTransform,
) : IrElementVisitorVoid, IrBuilder {

    lateinit var property: IrProperty
    lateinit var type: IrType
    lateinit var schemaField: IrVarargElement

    val callChain = mutableListOf<IrCall>()

    var nullable = false

    override fun visitProperty(declaration: IrProperty) {

        property = declaration

        val backingField = checkNotNull(declaration.backingField) { "missing backing field" }

        // this is the call to provideDelegate
        // it's first argument is the call to the type function (`int`, `string` etc.)

        val provideDelegateCall = checkNotNull(backingField.initializer?.expression) { "missing backing field expression" }
        check(provideDelegateCall is IrCall) { "backing field expression is not a call" }

        destructCallChain(provideDelegateCall)
        processCallChain()

        buildSchemaField()
    }

    fun destructCallChain(provideDelegateCall: IrCall) {
        // this is one of:
        //   - a call to the FDF function `int`, `string`, etc.
        //   - a call to an DTF function such as `nullable`

        var nextCall: IrExpression? = provideDelegateCall.receiverAndArgs().first()
        check(nextCall is IrCall) { "delegate provider argument is not a call" }

        var currentCall: IrCall = nextCall
        var callType = pluginContext.funCache[currentCall]

        while (callType == SchematicFunctionType.DefinitionTransform) {
            callChain += currentCall

            nextCall = currentCall.extensionReceiver
            check(nextCall != null && nextCall is IrCall) { "delegation call chain contains a non-call receiver" }

            currentCall = nextCall
            callType = pluginContext.funCache[currentCall]
        }

        check(callType == SchematicFunctionType.FieldDefinition) { "delegation call chain does not end with an FDF call" }
        callChain += currentCall
    }

    fun processCallChain() {
        // this is a bit hackish, but it will work for now
        for (call in callChain) {
            if (call.symbol.owner.name.identifier == "nullable") {
                nullable = true
            }
        }
    }

    fun buildSchemaField() {
        val fdfCall = callChain.last()
        val valueArguments = fdfCall.valueArguments
        val fieldClass = pluginContext.funCache.getFieldClass(fdfCall.symbol)

        schemaField =
            IrConstructorCallImpl(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                fieldClass.type,
                fieldClass.constructor,
                0, 0,
                2 + valueArguments.size // +2 = field name + nullable
            ).also { constructorCall ->
                constructorCall.putValueArgument(FIELD_CONSTRUCTOR_NAME_INDEX, irConst(property.name.identifier))
                constructorCall.putValueArgument(FIELD_CONSTRUCTOR_NULLABLE_INDEX, irConst(nullable))

                // TODO add a parameter name and type match check to SchemaField builder, should cache it probably
                for (i in valueArguments.indices) {
                    constructorCall.putValueArgument(FIELD_CONSTRUCTOR_VARARG_INDEX + i, valueArguments[i])
                }
            }
    }
}

