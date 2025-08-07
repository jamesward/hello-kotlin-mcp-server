plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
}

group = "com.jamesward"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("io.modelcontextprotocol.sdk:mcp:0.11.1")
    implementation("org.eclipse.jetty:jetty-server:11.0.18")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.18")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.9")
}

application {
    mainClass = "com.jamesward.hellokotlinmcpserver.ApplicationKt"
}

// note that the description & group make it show up in the gradle tasks so that the Heroku Buildpack works
tasks.register("stage") {
    description = "Prepare the application for deployment"
    group = "deployment"
    dependsOn("installDist")
}
