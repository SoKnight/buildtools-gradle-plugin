# BuildTools Gradle Plugin
Brings the Spigot build with BuildTools into your Gradle project

## Getting started
To use the plugin, apply the following two steps:

### 1) Apply the plugin
Kotlin DSL:
```kotlin
plugins {
    id("io.github.soknight.buildtools") version "1.0"
}
```

Groovy DSL:
```groovy
plugins {
    id 'io.github.soknight.buildtools' version '1.0'
}
```

### 2) Configure the plugin
Kotlin DSL:
```kotlin
buildTools {
    buildNumber = 193   // the latest on September 7th, 2025
    buildVersions.addAll("1.21.4", "1.21.5", "1.21.8")
}
```

Groovy DSL:
```groovy
buildTools {
    buildNumber = 193   // the latest on September 7th, 2025
    buildVersions = [ '1.21.4', '1.21.5', '1.21.8' ]
}
```

## Use-cases

### Enumerated versions & Aggregating task
You can enumerate all Spigot versions you want to build with BuildTools inside the `buildTools` extension:
```kotlin
buildTools {
    buildVersions.addAll("1.21.4", "1.21.5", "1.21.8")
}
```

In this case the plugin will automatically create `fetchBuildInfo@<version>` and `buildSpigot@<version>` tasks for every listed version.
Also plugin will create aggregating task `buildAllSpigot`, which may be used to build all listed versions sequentially:
```bash
./gradlew buildAllSpigot
```

Of course, plugin will not register these tasks if there are no `buildVersions` will be declared via `buildTools` extension.

### Manual tasks declaration
`FetchBuildInfoTask` and `BuildSpigotTask` may be registered manually, like this:
```kotlin
tasks.register<FetchBuildInfoTask>("fetchBuildInfo") {
    // declare Spigot version you want to build here
    buildVersion = "1.21.5"
}

tasks.register<BuildSpigotTask>("buildSpigot") {
    // use shorthand method to link it to fetched build info
    useFetchBuildInfoTask("fetchBuildInfo")
}
```
Any `BuildSpigotTask` task depends on the implicit task `setupBuildTools`, which downloads the BuildTools JAR and places it in the working directory.
You can use `setupBuildTools` task for any other purposes.
