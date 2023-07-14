/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.jvm.ir.receiverAndArgs
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addElement
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

class SchematicClassTransform(
    private val pluginContext: SchematicPluginContext
) : IrElementTransformerVoidWithContext() {

    val irContext = pluginContext.irContext
    val irFactory = pluginContext.irContext.irFactory
    val irBuiltIns = pluginContext.irContext.irBuiltIns

    val fields = mutableListOf<SchemaFieldBuilder>()
    lateinit var companionClass: IrClass

    override fun visitClassNew(declaration: IrClass): IrStatement {

        if (!declaration.superTypes.contains(pluginContext.schematicClass.typeWith(declaration.defaultType))) {
            return declaration
        }

        super.visitClassNew(declaration)

        addCompanion(declaration)
        addSchema()

        return declaration
    }

    override fun visitPropertyNew(declaration: IrProperty): IrStatement {
        if (!declaration.isDelegated) return declaration
        val backingField = declaration.backingField ?: return declaration

        // TODO checks and feedbacks in visitPropertyNew (non-schematic delegate?)

        // this is the call to provideDelegate
        // it's first argument is the call to the type function (`int`, `string` etc.)

        val provideDelegateCall = backingField.initializer?.expression ?: return declaration
        if (provideDelegateCall !is IrCall) return declaration

        // this is the call to the type function (`int`, `string`, etc)
        // it's parameters are the field constraints

        val typeFunctionCall = provideDelegateCall.receiverAndArgs().first()
        if (typeFunctionCall !is IrCall) return declaration

        fields += SchemaFieldBuilder(typeFunctionCall)


    }

    // ---------------------------------------------------------------------
    // Companion - add if not existing
    // ---------------------------------------------------------------------

    private fun addCompanion(declaration: IrClass) {
        val existing = declaration.companionObject()
        if (existing != null) {
            companionClass = existing
            return
        }

        companionClass = irFactory.buildClass {
            startOffset = declaration.endOffset
            endOffset = declaration.endOffset
            origin = IrDeclarationOrigin.DEFINED
            name = Name.identifier(COMPANION)
            kind = ClassKind.OBJECT
            visibility = DescriptorVisibilities.PUBLIC
            modality = Modality.FINAL
            isCompanion = true
        }

        companionClass.parent = declaration
        companionClass.superTypes = listOf(irBuiltIns.anyType)

        companionClass.addThisReceiver()
        companionClass.addPrimaryConstructor()
        companionClass.addFakeOverrides(pluginContext.typeSystem)

        declaration.declarations += companionClass
    }

    private fun IrClass.addThisReceiver(): IrValueParameter =

        irFactory.createValueParameter(
            SYNTHETIC_OFFSET,
            SYNTHETIC_OFFSET,
            IrDeclarationOrigin.INSTANCE_RECEIVER,
            IrValueParameterSymbolImpl(),
            SpecialNames.THIS,
            UNDEFINED_PARAMETER_INDEX,
            IrSimpleTypeImpl(symbol, false, emptyList(), emptyList()),
            varargElementType = null,
            isCrossinline = false,
            isNoinline = false,
            isHidden = false,
            isAssignable = false
        ).also {
            it.parent = this@addThisReceiver
            this@addThisReceiver.thisReceiver = it
        }

    private fun IrClass.addPrimaryConstructor(): IrConstructor =

        addConstructor {
            isPrimary = true
            visibility = DescriptorVisibilities.PRIVATE
            returnType = this@addPrimaryConstructor.typeWith()
        }.apply {
            parent = this@addPrimaryConstructor

            body = irFactory.createBlockBody(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).apply {

                statements += IrDelegatingConstructorCallImpl.fromSymbolOwner(
                    SYNTHETIC_OFFSET,
                    SYNTHETIC_OFFSET,
                    irBuiltIns.anyType,
                    irBuiltIns.anyClass.constructors.first(),
                    typeArgumentsCount = 0,
                    valueArgumentsCount = 0
                )

                statements += IrInstanceInitializerCallImpl(
                    SYNTHETIC_OFFSET,
                    SYNTHETIC_OFFSET,
                    this@addPrimaryConstructor.symbol,
                    irBuiltIns.unitType
                )
            }
        }

    // ---------------------------------------------------------------------
    // Companion - add the `schematicSchema` property
    // ---------------------------------------------------------------------

    fun addSchema() {
        companionClass.addIrProperty(
            Name.identifier(SCHEMATIC_SCHEMA_PROPERTY),
            pluginContext.schemaClass.defaultType,
            buildSchemaInitializer()
        )
    }

    fun IrClass.addIrProperty(
        inName: Name,
        inType: IrType,
        inInitializer: IrExpression? = null,
        overridden: List<IrPropertySymbol>? = null
    ): IrProperty {
        val irClass = this

        val irField = irFactory.buildField {
            name = inName
            type = inType
            origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
            visibility = DescriptorVisibilities.PRIVATE
        }.apply {
            parent = irClass
            initializer = inInitializer?.let { irFactory.createExpressionBody(it) }
        }

        val irProperty = irClass.addProperty {
            name = inName
            isVar = false
        }.apply {
            parent = irClass
            backingField = irField
            overridden?.let { overriddenSymbols = it }
            addDefaultGetter(irClass, irBuiltIns)
        }

        return irProperty
    }

    fun buildSchemaInitializer(): IrExpression =

        IrConstructorCallImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            pluginContext.schemaClass.defaultType,
            pluginContext.schemaClass.owner.primaryConstructor!!.symbol,
            0, 0,
            1 // array of fields
        ).also { constructorCall ->
            constructorCall.putValueArgument(0, buildFragmentVarArg())
        }

    fun buildFragmentVarArg(): IrExpression {
        return IrVarargImpl(
            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
            irBuiltIns.arrayClass.typeWith(pluginContext.schemaFieldType),
            pluginContext.schemaFieldType
        ).also { vararg ->
            fields.forEach { fieldTransform ->
                vararg.addElement(fieldTransform.buildNewFieldInstance())
            }
        }
    }

}
