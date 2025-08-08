rootProject.name = "hello-kotlin-mcp-server"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        maven("https://maven-central.storage.googleapis.com/maven2/")
        maven("https://central.sonatype.com/repository/maven-snapshots")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven-central.storage.googleapis.com/maven2/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}
