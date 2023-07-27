package hu.simplexion.z2.schematic.kotlin.ir.companion

import hu.simplexion.z2.schematic.kotlin.ir.ENCODE_PROTO
import hu.simplexion.z2.schematic.kotlin.ir.ENCODE_PROTO_VALUE_NAME
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
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.name.Name

class ProtoEncode(
    override val pluginContext: SchematicPluginContext,
    val companionTransform: CompanionTransform,
) : IrBuilder {

    val schematicClass = companionTransform.transformedClass
    val companionClass = companionTransform.companionClass

    fun build() {
        val existing = companionClass.getSimpleFunction(ENCODE_PROTO)?.owner

        when {
            existing == null -> add()
            existing.isFakeOverride -> transformFake(existing)
            else -> Unit // manually written
        }
    }

    fun add() {
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

            function.overriddenSymbols = listOf(pluginContext.schematicCompanionEncodeProto)

            function.addDispatchReceiver {
                type = companionClass.defaultType
            }

            function.addValueParameter(ENCODE_PROTO_VALUE_NAME, schematicClass.defaultType)
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

            val schema = irCall(
                companionTransform.companionSchematicSchemaGetter,
                dispatchReceiver = irGetObject(companionClass.symbol)
            )

            +irReturn(
                irCall(
                    pluginContext.schemaEncodeProto,
                    dispatchReceiver = schema,
                    args = arrayOf(irGet(valueParameters[0]))
                )
            )
        }
    }
}