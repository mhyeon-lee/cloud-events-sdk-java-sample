import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.cloudevents:cloudevents-core:2.4.1")
    implementation("io.cloudevents:cloudevents-api:2.4.1")
    implementation("io.cloudevents:cloudevents-json-jackson:2.4.1")
    implementation("io.cloudevents:cloudevents-kafka:2.4.1")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
