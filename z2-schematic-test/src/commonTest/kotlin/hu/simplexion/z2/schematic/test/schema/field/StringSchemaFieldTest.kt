package hu.simplexion.z2.schematic.test.schema.field

import hu.simplexion.z2.schematic.runtime.schema.field.StringSchemaField
import kotlin.test.Test
import kotlin.test.assertEquals

class StringSchemaFieldTest {

    @Test
    fun validateMinLength() {
        val field = StringSchemaField("n", false, null, 2, null, null, null)

        field.validate("").also { result ->
            assertEquals(false, result.valid)
            assertEquals(1, result.fails.size)
        }

        field.validate("1").also { result ->
            assertEquals(false, result.valid)
            assertEquals(1, result.fails.size)
        }

        field.validate("12").also { result ->
            assertEquals(true, result.valid)
            assertEquals(0, result.fails.size)
        }
    }

    @Test
    fun validateMaxLength() {
        val field = StringSchemaField("n", false, null, null, 2, null, null)

        field.validate("").also { result ->
            assertEquals(true, result.valid)
            assertEquals(0, result.fails.size)
        }

        field.validate("12").also { result ->
            assertEquals(true, result.valid)
            assertEquals(0, result.fails.size)
        }

        field.validate("123").also { result ->
            assertEquals(false, result.valid)
            assertEquals(1, result.fails.size)
        }
    }

    @Test
    fun validateBlank() {
        val field = StringSchemaField("n", false, null, null, null, false, null)

        field.validate("").also { result ->
            assertEquals(false, result.valid)
            assertEquals(1, result.fails.size)
        }

        field.validate("  ").also { result ->
            assertEquals(false, result.valid)
            assertEquals(1, result.fails.size)
        }

        field.validate("12").also { result ->
            assertEquals(true, result.valid)
            assertEquals(0, result.fails.size)
        }
    }

    @Test
    fun validateRegex() {
        val field = StringSchemaField("n", false, null, null, null, null, Regex("[0-9]"))

        field.validate("").also { result ->
            assertEquals(false, result.valid)
            assertEquals(1, result.fails.size)
        }

        field.validate("  ").also { result ->
            assertEquals(false, result.valid)
            assertEquals(1, result.fails.size)
        }

        field.validate("1").also { result ->
            assertEquals(true, result.valid)
            assertEquals(0, result.fails.size)
        }
    }

    @Test
    fun validateAllFail() {
        val field = StringSchemaField("n", false, null, 5, 0, false, Regex("[0-9]"))

        field.validate("  ").also { result ->
            assertEquals(false, result.valid)
            assertEquals(4, result.fails.size)
        }
    }

    @Test
    fun validateAllSuccess() {
        val field = StringSchemaField("n", false, null, 2, 4, false, Regex("[0-9]{2,4}"))

        field.validate("123").also { result ->
            assertEquals(true, result.valid)
            assertEquals(0, result.fails.size)
        }
    }

}