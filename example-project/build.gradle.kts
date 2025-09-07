plugins {
    java
    alias(libs.plugins.buildtools.gradle.plugin)
}

group = "io.github.soknight"
version = "1.0"

buildTools {
    buildNumber = 193   // the latest on September 7th, 2025
}