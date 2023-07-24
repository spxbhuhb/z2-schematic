/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import hu.simplexion.z2.schematic.kotlin.ir.util.SchematicFunctionCache
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class SchematicPluginContext(
    val irContext: IrPluginContext,
) {

    val schematicClass = SCHEMATIC_CLASS.runtimeClass()
    val schematicToAccessContext = checkNotNull(schematicClass.getSimpleFunction(SCHEMATIC_TO_ACCESS_CONTEXT)) { "missing Schematic.toSchematicAccessContext"}

    val schemaClass = SCHEMA_CLASS.runtimeClass(RUNTIME_SCHEMA_PACKAGE)
    val schemaInitWithDefaults = checkNotNull(schemaClass.getSimpleFunction("initWithDefaults")?.owner?.symbol) { "missing Schema.initWithDefault" }
    val schemaGetField = checkNotNull(schemaClass.getSimpleFunction("getField")?.owner?.symbol) { "Schema.getField is missing" }
    val schemaEncodeProto = checkNotNull(schemaClass.functionByName(ENCODE_PROTO).owner.symbol)
    val schemaDecodeProto = checkNotNull(schemaClass.functionByName(DECODE_PROTO).owner.symbol)

    val fdfAnnotationConstructor = FIELD_DEFINITION_FUNCTION_CLASS.runtimeClass().owner.constructors.first().symbol
    val dtfAnnotationConstructor = DEFINITION_TRANSFORM_FUNCTION_CLASS.runtimeClass().owner.constructors.first().symbol
    val safAnnotationConstructor = SCHEMATIC_ACCESS_FUNCTION_CLASS.runtimeClass().owner.constructors.first().symbol

    val schemaFieldClass = SCHEMA_FIELD_CLASS.runtimeClass(RUNTIME_SCHEMA_PACKAGE)
    val schemaFieldType = schemaFieldClass.defaultType

    val schematicCompanionClass = SCHEMATIC_COMPANION_CLASS.runtimeClass()
    val schematicCompanionSchematicSchema = schematicCompanionClass.propertySymbol(SCHEMATIC_SCHEMA_PROPERTY)
    val schematicCompanionEncodeProto = schematicCompanionClass.functionByName(ENCODE_PROTO)
    val schematicCompanionDecodeProto = schematicCompanionClass.functionByName(DECODE_PROTO)

    val schematicAccessContextClass = SCHEMATIC_ACCESS_CONTEXT.runtimeClass(RUNTIME_CONTEXT_PACKAGE).owner

    val mutableMapGet = irContext.irBuiltIns.mutableMapClass.functionByName("get").owner.symbol

    val typeSystem = IrTypeSystemContextImpl(irContext.irBuiltIns)

    val funCache = SchematicFunctionCache(this)

    val protoMessageType = PROTO_MESSAGE_CLASS.runtimeClass(PROTO_PACKAGE).defaultType

    val protoEncoderClassSymbol = PROTO_ENCODER_CLASS.runtimeClass(PROTO_PACKAGE)
    val protoDecoderClassSymbol = PROTO_DECODER_CLASS.runtimeClass(PROTO_PACKAGE)

    fun String.runtimeClass(pkg: String = RUNTIME_PACKAGE) =
        checkNotNull(irContext.referenceClass(ClassId(FqName(pkg), Name.identifier(this)))) {
            "Missing runtime class: $pkg.$this. Maybe the gradle dependency on \"hu.simplexion.z2:z2-schematic-runtime\" is missing."
        }

    fun IrClassSymbol.propertySymbol(name : String) =
        owner.properties.first { it.name.identifier == name }.symbol

}