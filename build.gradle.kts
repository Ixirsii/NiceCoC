import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"

    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ksp)

    application
    jacoco
}

group = "com.nicecoc"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Detekt plugins
    detektPlugins(libs.detekt.formatting)

    // KSP
    ksp(libs.ksp)

    // Clash API
    implementation(libs.clash.api)
    // Discord4J
    implementation(libs.discord4j.core)
    // Google Guava
    implementation(libs.guava)
    // Jackson
    implementation(libs.bundles.jackson)
    // Koin
    implementation(libs.bundles.koin)
    // Kotlin coroutines
    implementation(libs.kotlinx.coroutines.core)
    // Logback
    implementation(libs.logback.classic)
    // Netty
    implementation(libs.netty.common)
    // SLF4J
    implementation(libs.slf4j.api)

    // Kotlin Test
    testImplementation(kotlin("test"))
    // JUnit
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.platform.launcher)
    // Koin Test
    testImplementation(libs.koin.test)
}

application {
    mainClass.set("com.nicecoc.MainKt")
}

kotlin {
    jvmToolchain(21)
}

// Use KSP Generated sources
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

tasks.detekt {
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.test {
    useJUnitPlatform()
}
