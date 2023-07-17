/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir.access

import hu.simplexion.z2.schematic.kotlin.ir.SCHEMATIC_ACCESS_CONTEXT_ARG_COUNT
import hu.simplexion.z2.schematic.kotlin.ir.SCHEMATIC_ACCESS_CONTEXT_FIELD_INDEX
import hu.simplexion.z2.schematic.kotlin.ir.SCHEMATIC_SCHEMA_PROPERTY
import hu.simplexion.z2.schematic.kotlin.ir.SchematicPluginContext
import hu.simplexion.z2.schematic.kotlin.ir.util.IrBuilder
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.*

class SchematicAccessCallTransform(
    override val pluginContext: SchematicPluginContext,
    val safCall: IrCall
) : IrBuilder {

    /**
     * ```
     * CALL 'public final fun testFun (context: hu.simplexion.z2.schematic.runtime.context.SchematicContext, accessor: kotlin.Function0<kotlin.Any?>): hu.simplexion.z2.schematic.runtime.schema.SchemaFieldType declared in foo.bar' type=hu.simplexion.z2.schematic.runtime.schema.SchemaFieldType origin=null
     *   accessor: FUN_EXPR type=kotlin.Function0<kotlin.Any?> origin=LAMBDA
     *     FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> () returnType:kotlin.Any?
     *       BLOCK_BODY
     *         RETURN type=kotlin.Nothing from='local final fun <anonymous> (): kotlin.Any? declared in foo.bar.box'
     *           CALL 'public final fun <get-intField> (): kotlin.Int? declared in foo.bar.Test' type=kotlin.Int? origin=GET_PROPERTY
     *             $this: GET_VAR 'val test: foo.bar.Test [val] declared in foo.bar.box' type=foo.bar.Test origin=null
     * ```
     */
    fun transform(): IrExpression {
        val lambda = safCall.getValueArgument(safCall.valueArgumentsCount - 1)
        check(lambda != null) { "missing accessor" }
        check(lambda is IrFunctionExpression) { "accessor is not a function expression" }
        check(lambda.origin == IrStatementOrigin.LAMBDA) { "accessor is not a lambda" }

        val localFun = lambda.function
        check(localFun.isLocal) { "accessor is not local" }

        val body = localFun.body
        checkNotNull(body) { "missing accessor body" }
        check(body.statements.size == 1) { "more than one statement in the accessor" }

        val returnStatement = body.statements.first()
        check(returnStatement is IrReturn) { "accessor statement is not return" }

        // this is the expression that returns with the field value, a call to the getter
        val returnValue = returnStatement.value
        check(returnValue is IrCall) { "accessor return value is not a call" }

        val property = returnValue.symbol.owner.correspondingPropertySymbol
        check(property != null) { "can't find accessed property" }

        val fieldName = property.owner.name.identifier
        val schematicGet = checkNotNull(returnValue.dispatchReceiver?.deepCopyWithSymbols()) { "missing accessor dispatch receiver" }

        setSchematicContext(fieldName, schematicGet)

        return safCall
    }

    private fun setSchematicContext(fieldName: String, schematicGet: IrExpression) {
        val type = schematicGet.type
        val schematicClass = checkNotNull(type.getClass()) { "missing schematic class: $type" }

        val getSchema = irCall(
            checkNotNull(schematicClass.getPropertyGetter(SCHEMATIC_SCHEMA_PROPERTY)) { "missing schematicSchema getter" },
            dispatchReceiver = schematicGet
        )

        val getField = irCall(
            pluginContext.schemaGetField,
            dispatchReceiver = getSchema,
            args = arrayOf(irConst(fieldName))
        )

        val contextInstance = IrConstructorCallImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            pluginContext.schematicAccessContextClass.defaultType,
            pluginContext.schematicAccessContextConstructor,
            0, 0,
            SCHEMATIC_ACCESS_CONTEXT_ARG_COUNT // field
        ).also { constructorCall ->
            constructorCall.putValueArgument(SCHEMATIC_ACCESS_CONTEXT_FIELD_INDEX, getField)
        }

        safCall.putValueArgument(safCall.valueArgumentsCount - 2, contextInstance)
    }

}
