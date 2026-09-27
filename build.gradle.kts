import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SourcesJar

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
    id("com.vanniktech.maven.publish") version "0.37.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.dokka") version "2.2.0"
}

group = "org.wikilayer"
version =
    requireNotNull(
        Regex("""^## \[v(\d+\.\d+\.\d+)]""", RegexOption.MULTILINE)
            .find(file("CHANGELOG.md").readText()),
    ) { "CHANGELOG.md has no released version heading" }.groupValues[1]

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
    implementation("io.heapy.kotaml:kotaml:0.111.0")

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

mavenPublishing {
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = SourcesJar.Sources(),
        ),
    )
    publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.PUBLISHED)
    if (!providers.gradleProperty("unsignedLocalPublish").isPresent) {
        signAllPublications()
    }
    coordinates("org.wikilayer", "wlmarkdown-kotlin", version.toString())
    pom {
        name.set("WLMarkdown for Kotlin")
        description.set("Recognises Wikilayer's markdown dialect and extracts reader-visible text.")
        url.set("https://github.com/wikilayer/wlmarkdown-kotlin")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("wikilayer")
                name.set("Wikilayer")
                url.set("https://github.com/wikilayer")
            }
        }
        scm {
            url.set("https://github.com/wikilayer/wlmarkdown-kotlin")
            connection.set("scm:git:https://github.com/wikilayer/wlmarkdown-kotlin.git")
            developerConnection.set("scm:git:ssh://git@github.com/wikilayer/wlmarkdown-kotlin.git")
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
