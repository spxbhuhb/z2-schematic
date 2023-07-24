package hu.simplexion.z2.schematic.kotlin.ir.klass

import hu.simplexion.z2.schematic.kotlin.ir.*
import hu.simplexion.z2.schematic.kotlin.ir.util.IrBuilder
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.name.Name

class SchematicProtoCoders(
    override val pluginContext: SchematicPluginContext,
    val classTransform: SchematicClassTransform,
) : IrBuilder {

    val schematicClass = classTransform.transformedClass
    val companionClass = classTransform.companionClass

    fun build() {
        if (companionClass.getSimpleFunction(ENCODE_PROTO) == null) {
            addProtoEncoderFunc()
        }
        if (companionClass.getSimpleFunction(DECODE_PROTO) == null) {
            addProtoDecoderFunc()
        }
    }

    fun addProtoEncoderFunc() {
        companionClass.addFunction {
            name = Name.identifier(ENCODE_PROTO)
            returnType = irBuiltIns.byteArray.defaultType
            modality = Modality.FINAL
            visibility = DescriptorVisibilities.PUBLIC
            isSuspend = false
            isFakeOverride = false
            isInline = false
            origin = IrDeclarationOrigin.DEFINED
        }.also { function ->

            function.addDispatchReceiver {
                type = companionClass.defaultType
            }

            function.overriddenSymbols = listOf(pluginContext.schematicCompanionEncodeProto)

            val value = function.addValueParameter(ENCODE_PROTO_VALUE_NAME, schematicClass.defaultType)

            function.body = DeclarationIrBuilder(irContext, function.symbol).irBlockBody {

                val schema = irCall(
                    classTransform.companionSchematicSchemaGetter,
                    dispatchReceiver = irGetObject(companionClass.symbol)
                )

                +irReturn(
                    irCall(
                        pluginContext.schemaEncodeProto,
                        dispatchReceiver = schema,
                        args = arrayOf(irGet(value))
                    )
                )
            }
        }
    }

    fun addProtoDecoderFunc() {
        companionClass.addFunction {
            name = Name.identifier(DECODE_PROTO)
            returnType = schematicClass.defaultType
            modality = Modality.FINAL
            visibility = DescriptorVisibilities.PUBLIC
            isSuspend = false
            isFakeOverride = false
            isInline = false
            origin = IrDeclarationOrigin.DEFINED
        }.also { function ->

            function.addDispatchReceiver {
                type = companionClass.defaultType
            }

            function.overriddenSymbols = listOf(pluginContext.schematicCompanionDecodeProto)

            val value = function.addValueParameter(DECODE_PROTO_MESSAGE_NAME, pluginContext.protoMessageType)

            function.body = DeclarationIrBuilder(irContext, function.symbol).irBlockBody {

                val instance = IrConstructorCallImpl(
                    SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                    schematicClass.defaultType,
                    schematicClass.constructors.first { it.isPrimary }.symbol,
                    0, 0, 0
                )

                val schema = irCall(
                    classTransform.companionSchematicSchemaGetter,
                    dispatchReceiver = irGetObject(companionClass.symbol)
                )

                +irReturn(
                    irCall(
                        pluginContext.schemaDecodeProto,
                        dispatchReceiver = schema,
                        args = arrayOf(instance, irGet(value))
                    )
                )
            }
        }
    }

}