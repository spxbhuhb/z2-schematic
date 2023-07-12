# Z2 Schema

[![Maven Central](https://img.shields.io/maven-central/v/hu.simplexion.z2/z2-rpc)](https://mvnrepository.com/artifact/hu.simplexion.z2/z2-schema)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
![Kotlin](https://img.shields.io/github/languages/top/spxbhuhb/z2-schema)

Define schemas for UI building, validation and communication. Part of [Z2](https://github.com/spxbhuhb/z2).

Status: **experimental**

## Schema Objects

```kotlin
object AccountSchema : Schema("e58ab6a9-14fd-4d09-9aa7-1b7b627fc683", localeFallback = locale) {
    val uuid by uuid()
    val name by string(min = 5, max = 50, blank = false, pattern = "\p{Print}{5,50}")
    val email by email()
}
```