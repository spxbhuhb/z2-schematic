/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import hu.simplexion.z2.schematic.kotlin.ir.util.SchematicFunctionCache
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class SchematicPluginContext(
    val irContext: IrPluginContext,
) {

    val schematicClass = SCHEMATIC_CLASS.runtimeClass()

    val schemaClass = SCHEMA_CLASS.runtimeClass(RUNTIME_SCHEMA_PACKAGE)

    val fdfAnnotationConstructor = FIELD_DEFINITION_FUNCTION_CLASS.runtimeClass().owner.constructors.first().symbol
    val dtfAnnotationConstructor = DEFINITION_TRANSFORM_FUNCTION_CLASS.runtimeClass().owner.constructors.first().symbol
    val safAnnotationConstructor = SCHEMATIC_ACCESS_FUNCTION_CLASS.runtimeClass().owner.constructors.first().symbol

    val schemaFieldClass = SCHEMA_FIELD_CLASS.runtimeClass(RUNTIME_SCHEMA_PACKAGE)
    val schemaFieldType = schemaFieldClass.defaultType
    val schemaGetField = checkNotNull(schemaClass.getSimpleFunction("getField")?.owner?.symbol) { "Schema.getField is missing" }

    val schematicAccessContextClass = SCHEMATIC_ACCESS_CONTEXT.runtimeClass(RUNTIME_CONTEXT_PACKAGE).owner
    val schematicAccessContextConstructor = schematicAccessContextClass.constructors.first().symbol

    val mutableMapGet = irContext.irBuiltIns.mutableMapClass.functionByName("get").owner.symbol

    val typeSystem = IrTypeSystemContextImpl(irContext.irBuiltIns)

    val funCache = SchematicFunctionCache(this)

    fun String.runtimeClass(pkg: String? = null) =
        checkNotNull(irContext.referenceClass(ClassId(FqName(pkg ?: RUNTIME_PACKAGE), Name.identifier(this)))) {
            "Missing runtime class. Maybe the gradle dependency on \"hu.simplexion.z2:z2-schematic-runtime\" is missing."
        }

}