package hu.simplexion.z2.schematic.kotlin.ir

import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrVarargElement

class SchemaFieldBuilder(
    val pluginContext : SchematicPluginContext,
    val fieldName : String,
    val delegateCall: IrCall
) {

    val delegateFun = delegateCall.symbol.owner

    fun buildNewFieldInstance(): IrVarargElement {
        TODO("Not yet implemented")
    }
}