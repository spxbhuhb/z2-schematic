/*
 * Copyright © 2022-2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package hu.simplexion.z2.schematic.kotlin.ir

import org.jetbrains.kotlin.extensions.AnnotationBasedExtension
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor

interface SchematicAnnotationBasedExtension : AnnotationBasedExtension {

    fun IrClass.isAnnotatedWithCounter(): Boolean =
        toIrBasedDescriptor().hasSpecialAnnotation(null)

}