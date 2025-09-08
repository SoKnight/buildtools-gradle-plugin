plugins {
    java
    alias(libs.plugins.buildtools.gradle.plugin)
}

group = "io.github.soknight"
version = "1.0"

buildTools {
    buildNumber = 193   // the latest on September 7th, 2025
    buildVersions.addAll("1.21.4", "1.21.5", "1.21.8")
}