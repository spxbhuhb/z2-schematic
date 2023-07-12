/*
 * Copyright © 2023, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import java.net.URI

plugins {
    kotlin("multiplatform") version "1.8.22"
    signing
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven {
        url = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}


group = "hu.simplexion.z2"
val z2_commons_version: String by project

kotlin {
    jvmToolchain(11)

    jvm {
        withJava()
    }

    js(IR) {
        browser()
        binaries.library()
    }

    sourceSets {
        commonMain {
            dependencies {
                api("hu.simplexion.z2:z2-commons:${z2_commons_version}")
            }
        }
    }
}

if (project.properties["z2.publish"] != null || System.getenv("Z2_PUBLISH") != null) {

    val publishSnapshotUrl = (System.getenv("Z2_PUBLISH_SNAPSHOT_URL") ?: project.findProperty("z2.publish.snapshot.url"))?.toString()
    val publishReleaseUrl = (System.getenv("Z2_PUBLISH_RELEASE_URL") ?: project.findProperty("z2.publish.release.url"))?.toString()
    val publishUsername = (System.getenv("Z2_PUBLISH_USERNAME") ?: project.findProperty("z2.publish.username"))?.toString()
    val publishPassword = (System.getenv("Z2_PUBLISH_PASSWORD") ?: project.findProperty("z2.publish.password"))?.toString()
    val isSnapshot = "SNAPSHOT" in project.version.toString()

    signing {
        if (project.properties["signing.keyId"] == null) {
            useGpgCmd()
        }
        sign(publishing.publications)
    }

    publishing {

        repositories {
            if (isSnapshot) {
                requireNotNull(publishSnapshotUrl) { throw IllegalStateException("publishing: missing snapshot url, define Z2_PUBLISH_SNAPSHOT_URL") }
                maven(publishSnapshotUrl) {
                    credentials {
                        username = publishUsername
                        password = publishPassword
                    }
                }
            } else {
                requireNotNull(publishReleaseUrl) { throw IllegalStateException("publishing: missing release url, define Z2_PUBLISH_RELEASE_URL") }
                maven(publishReleaseUrl) {
                    credentials {
                        username = publishUsername
                        password = publishPassword
                    }
                }
            }
        }

        publications.withType<MavenPublication>().all {
            val path = "spxbhuhb/${project.name}"

            pom {
                description.set(project.name)
                name.set(project.name)
                url.set("https://github.com/$path")
                scm {
                    url.set("https://github.com/$path")
                    connection.set("scm:git:git://github.com/$path.git")
                    developerConnection.set("scm:git:ssh://git@github.com/$path.git")
                }
                licenses {
                    license {
                        name.set("proprietary")
                        distribution.set("repo")
                    }
                }
            }
        }
    }
}