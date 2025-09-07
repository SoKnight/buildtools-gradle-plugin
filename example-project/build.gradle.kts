import io.github.soknight.gradle.buildtools.task.BuildSpigotTask
import io.github.soknight.gradle.buildtools.task.FetchBuildInfoTask

plugins {
    java
    alias(libs.plugins.buildtools.gradle.plugin)
}

group = "io.github.soknight"
version = "1.0"

buildTools {
    buildNumber = 193   // the latest on September 7th, 2025
}

tasks.register<FetchBuildInfoTask>("fetchBuildInfo") {
    buildVersion = "1.21.5"
}

tasks.register<BuildSpigotTask>("buildSpigot") {
    useFetchBuildInfoTask("fetchBuildInfo")
}