plugins {
    java
    alias(libs.plugins.gradle.publish.plugin)
}

group = "io.github.soknight"
version = "1.0"

gradlePlugin {
    vcsUrl = "https://github.com/SoKnight/buildtools-gradle-plugin.git"
    website = "https://github.com/SoKnight/buildtools-gradle-plugin"

    plugins {
        create("buildtools-gradle-plugin", fun PluginDeclaration.() {
            id = "io.github.soknight.buildtools"
            displayName = "BuildTools Gradle Plugin"
            description = "Brings the Spigot build with BuildTools into your Gradle project"
            tags = listOf("minecraft", "spigot", "buildtools")
            implementationClass = "io.github.soknight.gradle.buildtools.BuildToolsPlugin"
        })
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jackson.databind)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

tasks.withType<Javadoc> {
    enabled = false
}