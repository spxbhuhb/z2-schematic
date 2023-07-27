/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir.klass

import hu.simplexion.z2.schematic.kotlin.ir.SchematicPluginContext
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType

class SchematicModuleTransform(
    private val pluginContext: SchematicPluginContext
) : IrElementTransformerVoidWithContext() {

    val classTransforms = mutableListOf<SchematicClassTransform>()

    override fun visitClassNew(declaration: IrClass): IrStatement {

        if (!declaration.superTypes.contains(pluginContext.schematicClass.typeWith(declaration.defaultType))) {
            return declaration
        }

        return declaration.accept(SchematicClassTransform(pluginContext).also { classTransforms += it }, null) as IrStatement
    }

    fun transformFields() {
        classTransforms.forEach { it.transformFields() }
    }

}
