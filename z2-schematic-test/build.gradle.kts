/*
 * Copyright © 2020-2021, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
plugins {
    kotlin("multiplatform") version "1.9.0"
    id("hu.simplexion.z2.schematic") version "2023.7.30-SNAPSHOT"
}

val z2_schematic_version: String by project

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm {}
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("hu.simplexion.z2:z2-schematic-runtime:${z2_schematic_version}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}