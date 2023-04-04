import groovy.xml.dom.DOMCategory.attributes
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "xyz.le30r"
version = "1.0-SNAPSHOT"

val ktor_version: String by project
val tgbotapi_version: String by project


repositories {
    mavenCentral()

    maven {
        url = uri("https://jitpack.io")
    }

}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf(
            "Main-Class" to "xyz.le30r.ApplicationKt"
        ))
    }
}

dependencies {
    implementation("dev.inmo:tgbotapi:$tgbotapi_version")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")

    implementation("io.ktor:ktor-client-java:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}


tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("open-ai-chatgpt.jar")

}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}