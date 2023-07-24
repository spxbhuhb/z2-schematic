package hu.simplexion.z2.schematic.runtime.schema.validation

import hu.simplexion.z2.commons.i18n.LocalizedTextStore
import hu.simplexion.z2.commons.util.UUID

@Suppress("ClassName")
object validationStrings : LocalizedTextStore(UUID("63080e45-1b3e-41f6-aaa9-0bc6f81e12cd")) {
    val nullFail by "Value required"
    val integerFail by "Integer value required"
    val minValueFail by "Value is less than %N"
    val maxValueFail by "Value is greater than %N"
    val minLengthFail by "At least %N characters required"
    val maxLengthFail by "Maximum %N characters allowed"
    val blankFail by "Blank value is not allowed"
    val booleanFail by "Boolean value required"
    val patternFail by "Invalid value"
    val uuidFail by "UUID value required"
    val nilFail by "Non-NUL UUID value required"
}