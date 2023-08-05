package hu.simplexion.z2.schematic.test.schema

import kotlin.test.Test
import kotlin.test.assertEquals

class ListenerTest {

    @Test
    fun testDirectChange() {
        var newValue = 0

        val a = A()
        a.schematicAddListener { thisRef, _ -> newValue = thisRef.i }

        a.i = 12
        assertEquals(12, newValue)

        a.i = 13
        assertEquals(13, newValue)
    }

    @Test
    fun testIndirectChange() {
        var newValue = 0

        val a = A()
        a.schematicAddListener { thisRef, _ -> newValue = thisRef.i }

        a.schematicChange(a.schematicSchema.getField("i"), 12)
        assertEquals(12, newValue)
    }

}