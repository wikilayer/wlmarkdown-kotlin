plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "org.wikilayer"
version = "0.6.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.commonmark:commonmark:0.30.0")
    api("org.commonmark:commonmark-ext-gfm-tables:0.30.0")
    api("org.commonmark:commonmark-ext-gfm-strikethrough:0.30.0")
    api("org.commonmark:commonmark-ext-task-list-items:0.30.0")
    implementation("io.heapy.kotaml:kotaml:0.108.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

ktlint {
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
}
