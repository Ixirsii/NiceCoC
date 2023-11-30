import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"

    application
    idea
    jacoco

    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "com.nicecoc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val clashApiVersion: String by project
val discord4JVersion: String by project
val dokkaVersion: String by project
val guavaVersion: String by project
val jacksonVersion: String by project
val junitVersion: String by project
val koinVersion: String by project
val koinKspVersion: String by project
val kotlinxVersion: String by project
val logbackVersion: String by project
val mockkVersion: String by project
val nettyVersion: String by project
val slf4JVersion: String by project

dependencies {
    // KSP
    ksp("io.insert-koin:koin-ksp-compiler:$koinKspVersion")
    // Clash API
    implementation("io.github.lycoon:clash-api:$clashApiVersion")
    // Discord4J
    implementation("com.discord4j:discord4j-core:$discord4JVersion")
    // Google Guava
    implementation("com.google.guava:guava:$guavaVersion")
    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    // Koin
    implementation("io.insert-koin:koin-annotations:$koinKspVersion")
    implementation("io.insert-koin:koin-core:$koinVersion")
    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")
    // Logback
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    // Netty
    implementation("io.netty:netty-common:$nettyVersion")

    // Kotlin Test
    testImplementation(kotlin("test"))
    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    // Koin Test
    testImplementation("io.insert-koin:koin-test:$koinVersion")
}

application {
    mainClass.set("com.nicecoc.MainKt")
}

// Use KSP Generated sources
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

tasks.test {
    useJUnitPlatform()
}
