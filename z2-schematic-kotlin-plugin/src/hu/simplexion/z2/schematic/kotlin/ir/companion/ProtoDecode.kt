package hu.simplexion.z2.schematic.kotlin.ir.companion

import hu.simplexion.z2.schematic.kotlin.ir.DECODE_PROTO
import hu.simplexion.z2.schematic.kotlin.ir.DECODE_PROTO_MESSAGE_NAME
import hu.simplexion.z2.schematic.kotlin.ir.SchematicPluginContext
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
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.name.Name

class ProtoDecode(
    override val pluginContext: SchematicPluginContext,
    val companionTransform: CompanionTransform,
) : IrBuilder {

    val schematicClass = companionTransform.transformedClass
    val companionClass = companionTransform.companionClass

    fun build() {
        val existing = companionClass.getSimpleFunction(DECODE_PROTO)?.owner

        when {
            existing == null -> add()
            existing.isFakeOverride -> transformFake(existing)
            else -> Unit // manually written
        }
    }

    fun add() {
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

            function.overriddenSymbols = listOf(pluginContext.schematicCompanionDecodeProto)

            function.addDispatchReceiver {
                type = companionClass.defaultType
            }

            function.addValueParameter(DECODE_PROTO_MESSAGE_NAME, pluginContext.protoMessageType.makeNullable())
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

            val instance = IrConstructorCallImpl(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                schematicClass.defaultType,
                schematicClass.constructors.first { it.isPrimary }.symbol,
                0, 0, 0
            )

            val schema = irCall(
                companionTransform.companionSchematicSchemaGetter,
                dispatchReceiver = irGetObject(companionClass.symbol)
            )

            + irReturn(
                irCall(
                    pluginContext.schemaDecodeProto,
                    dispatchReceiver = schema,
                    args = arrayOf(instance, irGet(valueParameters[0]))
                )
            )
        }
    }
}