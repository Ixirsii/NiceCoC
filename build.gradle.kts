import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"

    application
    idea
    jacoco

    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    id("org.jetbrains.dokka") version "1.5.31"
}

group = "com.nicecoc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        url = uri("https://maven.pkg.github.com/Lycoon/clash-api")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    // KSP
    ksp("io.insert-koin:koin-ksp-compiler:1.0.1")
    // Clash API
    implementation("com.lycoon:clashapi:3.0.2")
    // Discord4J
    implementation("com.discord4j:discord4j-core:3.2.3")
    // Google Guava
    implementation("com.google.guava:guava:31.1-jre")
    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.13.4")
    // Koin
    implementation("io.insert-koin:koin-annotations:1.0.1")
    implementation("io.insert-koin:koin-core:3.2.0")
    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // Logback
    implementation("ch.qos.logback:logback-classic:1.4.0")
    // Netty
    implementation("io.netty:netty-common:4.1.81.Final")

    // Kotlin Test
    testImplementation(kotlin("test"))
    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    // Koin Test
    testImplementation("io.insert-koin:koin-test:3.2.0")
    implementation(kotlin("stdlib-jdk8"))
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

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
