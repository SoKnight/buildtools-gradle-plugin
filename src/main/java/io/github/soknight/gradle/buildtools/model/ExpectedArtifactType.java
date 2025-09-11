package io.github.soknight.gradle.buildtools.model;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

@AllArgsConstructor
public enum ExpectedArtifactType {

    MAPPINGS_MOJANG             ("minecraft-server",    "maps-mojang",          "txt"),
    MAPPINGS_SPIGOT             ("minecraft-server",    "maps-spigot",          "csrg"),
    MINECRAFT_SERVER_JAR        ("minecraft-server"),
    SPIGOT_API_JAR              ("spigot-api"),
    SPIGOT_JAR                  ("spigot"),
    SPIGOT_REMAPPED_MOJANG_JAR  ("spigot",              "remapped-mojang"),
    SPIGOT_REMAPPED_OBF_JAR     ("spigot",              "remapped-obf"),
    ;

    private final @NotNull String artifactId;
    private final @Nullable String classifier;
    private final @Nullable String extension;

    ExpectedArtifactType(@NotNull String artifactId) {
        this(artifactId, null, null);
    }

    ExpectedArtifactType(@NotNull String artifactId, @Nullable String classifier) {
        this(artifactId, classifier, null);
    }

    public boolean requiresBuildWithRemappedOption() {
        return this != MINECRAFT_SERVER_JAR && this != SPIGOT_API_JAR && this != SPIGOT_JAR;
    }

    public @NotNull Path repoPath(@NotNull String minecraftSnapshot, @NotNull String spigotSnapshot) {
        return Path.of(urlPath(minecraftSnapshot, spigotSnapshot).replace('/', File.separatorChar));
    }

    public @NotNull String urlPath(@NotNull String minecraftSnapshot, @NotNull String spigotSnapshot) {
        var version = effectiveSnapshot(minecraftSnapshot, spigotSnapshot);

        var builder = new StringBuilder("org/spigotmc");
        builder.append('/').append(artifactId);
        builder.append('/').append(version);
        builder.append('/').append(artifactId).append('-').append(version);

        if (classifier != null)
            builder.append('-').append(classifier);

        builder.append('.').append(extension != null ? extension : "jar");
        return builder.toString();
    }

    private @NotNull String effectiveSnapshot(@NotNull String minecraft, @NotNull String spigot) {
        return "minecraft-server".equals(artifactId) ? minecraft : spigot;
    }

}
