/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir.companion

import hu.simplexion.z2.schematic.kotlin.ir.SCHEMATIC_COMPANION_GET_FIELD_VALUE
import hu.simplexion.z2.schematic.kotlin.ir.SchematicPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.IrType

class GetFieldValue(
    pluginContext: SchematicPluginContext,
    companionTransform: CompanionTransform,
) : AbstractCompanionFun(
    pluginContext,
    companionTransform,
    SCHEMATIC_COMPANION_GET_FIELD_VALUE,
    pluginContext.schematicCompanionGetFieldValue
) {

    override val returnType: IrType
        get() = irBuiltIns.anyNType

    override fun IrSimpleFunction.buildBody() {
        body = DeclarationIrBuilder(irContext, this.symbol).irBlockBody {

        }
    }

}
