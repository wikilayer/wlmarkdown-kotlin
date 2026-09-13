plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

// JitPack builds this repository under com.github.wikilayer at the name of the tag,
// and Gradle refuses a module whose POM carries other coordinates than the ones it
// asked for. So the group is the one JitPack serves, and the version is whatever it
// passes in, falling back to the tag this branch is heading for.
group = "com.github.wikilayer"
if (version == Project.DEFAULT_VERSION) version = "v0.6.0"

repositories {
    mavenCentral()
}

dependencies {
    api("org.commonmark:commonmark:0.30.0")
    api("org.commonmark:commonmark-ext-autolink:0.30.0")
    api("org.commonmark:commonmark-ext-gfm-tables:0.30.0")
    api("org.commonmark:commonmark-ext-gfm-strikethrough:0.30.0")
    api("org.commonmark:commonmark-ext-task-list-items:0.30.0")
    // Found is @Serializable, so whoever holds one needs the annotations on their
    // own compile classpath rather than only on ours.
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
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

// No javadoc jar: nothing here builds documentation, and an empty one published
// beside the sources would only promise a reference that does not exist.
java {
    withSourcesJar()
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
