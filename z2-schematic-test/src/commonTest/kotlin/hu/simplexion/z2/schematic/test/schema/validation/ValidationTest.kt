package hu.simplexion.z2.schematic.test.schema.validation

import hu.simplexion.z2.schematic.test.schema.A
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationTest {

    @Test
    fun testValid() {
        val s = A().apply {
            i = 100
        }
        assertTrue { s.isValid }
    }

    @Test
    fun testInvalid() {
        val s = A().apply {
            i = 99
        }
        assertFalse { s.isValid }
    }
}