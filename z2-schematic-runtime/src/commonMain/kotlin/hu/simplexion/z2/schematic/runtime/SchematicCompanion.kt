package hu.simplexion.z2.schematic.runtime

import hu.simplexion.z2.commons.protobuf.ProtoDecoder
import hu.simplexion.z2.commons.protobuf.ProtoEncoder
import hu.simplexion.z2.schematic.runtime.schema.Schema

interface SchematicCompanion<T> : ProtoEncoder<T>, ProtoDecoder<T> {

    val schematicSchema : Schema

}