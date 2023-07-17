/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import hu.simplexion.z2.schematic.kotlin.ir.access.SchematicAccessTransform
import hu.simplexion.z2.schematic.kotlin.ir.klass.SchematicModuleTransform
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class SchematicGenerationExtension: IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        SchematicPluginContext(pluginContext).apply {

            moduleFragment.accept(SchematicModuleTransform(this), null)
            moduleFragment.accept(SchematicAccessTransform(this), null)

        }
    }
}

