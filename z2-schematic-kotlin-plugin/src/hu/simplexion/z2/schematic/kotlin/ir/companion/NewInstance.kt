/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir.companion

import hu.simplexion.z2.schematic.kotlin.ir.SCHEMATIC_COMPANION_NEW_INSTANCE
import hu.simplexion.z2.schematic.kotlin.ir.SchematicPluginContext
import hu.simplexion.z2.schematic.kotlin.ir.util.IrBuilder
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name

/**
 * Transform the `newInstance` function of the companion. The function
 * may be:
 *
 * - missing, when the companion class is created by the plugin - add
 * - fake override, when the companion is declared but the property is not overridden - convert
 * - override, when there is an actual implementation - do not touch
 */
class NewInstance(
    override val pluginContext: SchematicPluginContext,
    companionTransform: CompanionTransform,
) : IrBuilder {

    val transformedClass = companionTransform.transformedClass
    val companionClass = companionTransform.companionClass

    fun build() {
        val existing = companionClass.getSimpleFunction(SCHEMATIC_COMPANION_NEW_INSTANCE)?.owner

        when {
            existing == null -> add()
            existing.isFakeOverride -> transformFake(existing)
            else -> Unit // manually written
        }
    }

    fun add() {
        companionClass.addFunction {
            name = Name.identifier(SCHEMATIC_COMPANION_NEW_INSTANCE)
            returnType = transformedClass.defaultType
            modality = Modality.FINAL
            visibility = DescriptorVisibilities.PUBLIC
            isSuspend = false
            isFakeOverride = false
            isInline = false
            origin = IrDeclarationOrigin.DEFINED
        }.also { function ->

            function.overriddenSymbols = listOf(pluginContext.schematicCompanionNewInstance)

            function.addDispatchReceiver {
                type = companionClass.defaultType
            }

            function.buildBody()
        }
    }

    fun transformFake(declaration: IrSimpleFunction) {
        declaration.origin = IrDeclarationOrigin.DEFINED
        declaration.isFakeOverride = false
        declaration.buildBody()
    }

    fun IrSimpleFunction.buildBody() {
        body = DeclarationIrBuilder(irContext, this.symbol).irBlockBody {
            + irReturn(
                IrConstructorCallImpl(
                    SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                    transformedClass.defaultType,
                    transformedClass.primaryConstructor !!.symbol,
                    0, 0, 0
                )
            )
        }
    }

}
