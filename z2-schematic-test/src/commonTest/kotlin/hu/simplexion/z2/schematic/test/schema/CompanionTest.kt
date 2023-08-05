package hu.simplexion.z2.schematic.test.schema

import kotlin.test.Test
import kotlin.test.assertEquals

class CompanionTest {

    @Test
    fun testInvoke() {
        val a = A {
            i = 12
        }
        assertEquals(12, a.i)
    }
}