/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class SchematicPluginContext(
    val irContext: IrPluginContext,
) {

    val schematicClass =
        checkNotNull(irContext.referenceClass(ClassId(FqName(RUNTIME_PACKAGE), Name.identifier(SCHEMATIC_CLASS)))) {
            "Missing Schematic. Maybe the gradle dependency on \"hu.simplexion.z2:z2-schematic-runtime\" is missing."
        }

    val schemaClass = checkNotNull(irContext.referenceClass(ClassId(FqName(RUNTIME_SCHEMA_PACKAGE), Name.identifier(SCHEMA_CLASS))))

    val schemaFieldClass = checkNotNull(irContext.referenceClass(ClassId(FqName(RUNTIME_SCHEMA_PACKAGE), Name.identifier(SCHEMA_FIELD_CLASS))))
    val schemaFieldType = schemaFieldClass.defaultType

    val typeSystem = IrTypeSystemContextImpl(irContext.irBuiltIns)

}

