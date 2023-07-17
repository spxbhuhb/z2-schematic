# Z2 Schematic

[![Maven Central](https://img.shields.io/maven-central/v/hu.simplexion.z2/z2-rpc)](https://mvnrepository.com/artifact/hu.simplexion.z2/z2-schema)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
![Kotlin](https://img.shields.io/github/languages/top/spxbhuhb/z2-schema)

Schematic classes for easy UI building, data management and communication. Part of [Z2](https://github.com/spxbhuhb/z2).

Status: **initial development**

**====  Important ====**

**The information below is the "specification" of how the library will work, there are parts that are not implemented yet.**

Schematic classes can:

* provide render information for the UI
* provide validation and user feedback for the UI
* create patches that contain only the changes
* apply patches to existing objects

The library has a runtime part and a Kotlin compiler plugin that transforms the code.

## Overview

When using schematic we work mostly with:

* schematic data classes
* schemas
* schematic access functions

The first two is quite straightforward, while the third is a bit tricky, but very powerful.

### Schematic Data Classes

Schematic data classes store the data the application handles:

```kotlin
class Book : Schematic<Book>() { 
    val title by string(maxLength = 100, blank = false)
    val authors by list<Author>(minSize = 1, maxSize = 10)
    val publicationDate by localDate(after = LocalDate(1970,1,1))
}
```

When you have a schematic class, you can:

* get and set the properties just as you do with any other class,
* get the changes made with the `schematicCollect` function
* apply the changes to another instance of the class with the `schematicPatch` function
* add event listeners which are called whenever a field changes
* validate the data with the `Schema` of the class

All fields are initialized to their "natural" default values. If you want a different
value use the `default` parameter.

  * int = 0
  * string = ""
  * nullable fields = null

### Schemas

The `Schema` is generated for the class automatically by the compiler plugin.
You can't see it in the editor, but it looks like this:

```kotlin
class Book : Schematic<Book>() {
    //. .. definition of fields as above
    companion object {
        val schematicSchema = Schema(
            StringSchemaField("title", maxLength = 100, blank = false),
            ListSchemaField("authors", minSize = 1, maxSize = 2, Author.schematicSchema),
            LocalDateSchemaField("publicationDate", after = LocalDate(1970,1,1))
        )
    }
    
}
```

You can:

* access the schema of a schematic class instance in the `schematicSchema` property
* validate a schematic data class instance with the `validate` and `suspendValidate` functions
* get the schema field for any schematic property automatically with schematic access functions

### Schematic Access Functions

The schematic access functions (SAF) are a bit tricky and one of the main reasons this library has been written.
Easiest way to explain how they work is by an example:

```kotlin
div { // let's assume this is part of a web page
    editor { book.title }
}
```

Here `editor` is a SAF. The trick is that when the compiler plugin sees a SAF call it fetches the schema field that
belongs to the property used between the brackets and passes it to the function called, along with the value.

So, at the end, the function call above turns into something like this:

```kotlin
div {
    editor(Book.schematicSchema.fields["title"]) { book.title }
}
```

When you write the `editor` SAF you have the metadata that belongs to the property between the brackets, in this case
the `title` property of the `Book` class.

This has many uses. For example, you can write an `editor` that can validate the field based on the schema
data.

You can define the `editor` SAF like shown below (details about that later).

Note the context parameter with the default value of `null`. This is the parameter that is set by the compiler
plugin to contain the metadata of the property used.

```kotlin
@SchematicAccessFunction
fun editor(context : SchematicAccessContext? = null, accessor : () -> Any?) {
    checkNotNull(context)
    when (context.field.type) {
        SchematicFieldType.String -> stringEditor(context)
        SchematicFieldType.LocalDate -> localDateEditor(context)
        else -> defaultEditor(context)
    }
}
```

## Details

### Schematic Classes

To define a schematic class, extend `Schematic` and use the provided field definition functions. You may find the [list
of available functions](#field-definition-functions) below or [define your own](#writing-field-definition-functions).

```kotlin
class Test : Schematic<Test>() {
    val intField by int(min = 5)
}
```

Is turned into:

```kotlin
class Test : Schematic<Test> {

    val intField : Int
        get() = schematicValues["intField"]!! as Int
        set(value) {
            schematicChangeInt("intField", value)
        }

    override val schematicSchema
        get() = Companion.schematicSchema

    companion object {
        val schematicSchema = Schema(
            IntSchemaField("intField", min = 5)
        )
    }
}
```

The schema is independent of the data instances, but any given instance can access its own schema through the
`schematicSchema` property.

### Field Definition Functions

```kotlin
fun boolean(
    default: Boolean = false
)
```

```kotlin
fun int(
    default : Int = 0,
    min : Int? = null,
    max : Int? = null
) : Int
```

```kotlin
fun string(
    default : String = "",
    minLength : Int? = null,
    maxLength : Int? = null,
    blank : Boolean = true,
    pattern : Regex? = null
)
```

### Writing Field Definition Functions

To define an FDF you need to:

1. create the function itself
2. create a schema field class that extends `SchemaField`
   1. All parameters of the FDF must be present as parameters of the field class constructor with the same name and same order after the overridden fields.

```kotlin
@Suppress("UNUSED_PARAMETER")
@FieldDefinitionFunction(IntSchemaField::class)
fun int(
    default: Int = 0,
    min: Int? = null,
    max: Int? = null
) = PlaceholderDelegateProvider<T,Int>()
```

```kotlin
class IntSchemaField(
    override val name: String,
    val default: Int = 0,
    val min: Int? = null,
    val max: Int? = null,
) : SchemaField {

    override val type: SchemaFieldType
        get() = SchemaFieldType.Int

   override fun toTypedValue(anyValue: Any?, fails: MutableList<ValidationFailInfo>): Int? {
      if (anyValue == null) return null

      return when (anyValue) {
         is Int -> anyValue
         is Number -> anyValue.toInt()
         is String -> anyValue.toIntOrNull()
         else -> {
            fails += fail(validationStrings.integerFail)
            null
         }
      }
   }

   override fun validateNotNullable(value: Int, fails: MutableList<ValidationFailInfo>) {
      if (min != null && value < min) fails += fail(validationStrings.minValueFail, min)
      if (max != null && value > max) fails += fail(validationStrings.maxValueFail, max)
   }
}
```

### Definition Transform Functions

As of now there is one definition transform function (DTF): `nullable`.
This transforms the field into a nullable one.