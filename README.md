# z2-schema

Define schemas for UI building, validation and communication.

## Schema Objects

```kotlin
object AccountSchema : Schema("e58ab6a9-14fd-4d09-9aa7-1b7b627fc683", localeFallback = locale) {
    val uuid by uuid()
    val name by string(min = 5, max = 50, blank = false, pattern = "\p{Print}{5,50}")
    val email by email()
}
```

## Schema Fields

Each property of a schema object is an instance of `SchemaField`. 