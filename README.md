# BuildTools Gradle Plugin
Brings the Spigot build with BuildTools into your Gradle project

## Getting started
To use the plugin, apply the following two steps:

### 1) Apply the plugin
Kotlin DSL:
```kotlin
plugins {
    id("io.github.soknight.buildtools") version "1.2"
}
```

Groovy DSL:
```groovy
plugins {
    id 'io.github.soknight.buildtools' version '1.2'
}
```

### 2) Configure the plugin
Kotlin DSL:
```kotlin
buildTools {
    buildToolsVersion = 193 // the latest on September 7th, 2025
    minecraftVersions.addAll("1.21.4", "1.21.5", "1.21.8")
}
```

Groovy DSL:
```groovy
buildTools {
    buildToolsVersion = 193 // the latest on September 7th, 2025
    minecraftVersions = [ '1.21.4', '1.21.5', '1.21.8' ]
}
```

## Use-cases

### Enumerated versions & Aggregating task
You can enumerate all Spigot versions you want to build with BuildTools inside the `buildTools` extension:
```kotlin
buildTools {
    minecraftVersions.addAll("1.21.4", "1.21.5", "1.21.8")
}
```

In this case the plugin will automatically create `fetchBuildInfo@<version>`, `fetchVersionInfo@<version>` and `buildSpigot@<version>` tasks for every listed version.
Also plugin will create aggregating task `buildAllSpigot`, which may be used to build all listed versions sequentially:
```bash
./gradlew buildAllSpigot
```

Of course, plugin will not register these tasks if there are no `minecraftVersions` will be declared via `buildTools` extension.

### Manual tasks declaration
`FetchBuildInfoTask`, `FetchVersionInfoTask` and `BuildSpigotTask` may be registered manually, like this:
```kotlin
tasks.register<FetchBuildInfoTask>("fetchBuildInfo") {
    // declare Minecraft version you want to build here
    minecraftVersion = "1.21.5"
}

tasks.register<FetchVersionInfoTask>("fetchVersionInfo") {
    // use shorthand method to link it to fetched build info
    useFetchBuildInfoTask("fetchBuildInfo")
}

tasks.register<BuildSpigotTask>("buildSpigot") {
    // use shorthand methods to link it to fetched build/version info
    useFetchBuildInfoTask("fetchBuildInfo")
    useFetchVersionInfoTask("fetchVersionInfo")
}
```
Any `BuildSpigotTask` task depends on the implicit task `setupBuildTools`, which downloads the BuildTools JAR and places it in the working directory.
You can use `setupBuildTools` task for any other purposes.
