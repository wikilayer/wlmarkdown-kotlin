plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.dokka") version "2.2.0"
}

// JitPack builds this repository under com.github.wikilayer at the name of the tag,
// and Gradle refuses a module whose POM carries other coordinates than the ones it
// asked for. So the group is the one JitPack serves, and the version is whatever it
// passes in, falling back to the tag this branch is heading for.
group = "com.github.wikilayer"
if (version == Project.DEFAULT_VERSION) version = "v0.8.0"

// The tests read the shared files where they lie rather than off the classpath,
// so they are told where the checkout is instead of guessing at a working directory.
val repositoryProperty = "wlmarkdown.repository"

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
    implementation("io.heapy.kotaml:kotaml:0.110.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

// The corpus of the leading port is not an input Gradle can see, so an unchanged
// checkout is up to date however far that corpus has moved since. The check that
// asks it therefore runs in a task of its own that is never up to date and never
// cached, which is the only way it answers the question it was written for.
val reachesTheLeadingPort = "reaches-the-leading-port"

tasks.test {
    useJUnitPlatform { excludeTags(reachesTheLeadingPort) }
    systemProperty(repositoryProperty, projectDir.absolutePath)
}

val corpusIsCurrent =
    tasks.register<Test>("corpusIsCurrent") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Asks the leading port whether this copy of the corpus is still its corpus."
        testClassesDirs =
            sourceSets.test
                .get()
                .output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        useJUnitPlatform { includeTags(reachesTheLeadingPort) }
        systemProperty(repositoryProperty, projectDir.absolutePath)
        outputs.upToDateWhen { false }
        outputs.cacheIf { false }
    }

tasks.check {
    dependsOn(corpusIsCurrent)
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
}

dokka {
    dokkaPublications.html {
        moduleName.set("WLMarkdown for Kotlin")
        moduleVersion.set(project.version.toString())
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
        includes.from("docs/module.md")
    }
    dokkaSourceSets.configureEach {
        sourceRoots.from(file("src/main/kotlin"))
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(uri("https://github.com/wikilayer/wlmarkdown-kotlin/tree/main/src/main/kotlin"))
            remoteLineSuffix.set("#L")
        }
    }
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
