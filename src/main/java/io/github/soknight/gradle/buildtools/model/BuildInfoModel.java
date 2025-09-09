package io.github.soknight.gradle.buildtools.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public record BuildInfoModel(
        @JsonProperty("description") String description,
        @JsonProperty("javaVersions") int[] rawJavaVersions,
        @JsonProperty("name") String name,
        @JsonProperty("refs") Refs gitRefs,
        @JsonProperty("toolsVersion") Integer buildToolsVersion
) {

    public static final int DEFAULT_JAVA_VERSION = 52;  // Java 8

    // Java LTS releases: 8, 11, 17, 21 and 25
    public static final @NotNull Set<Integer> JAVA_LTS_RELEASES = Set.of(52, 55, 61, 65, 69);

    public int minSupportedJavaVersion() {
        return rawJavaVersions != null && rawJavaVersions.length == 2
                ? IntStream.of(rawJavaVersions).min().orElse(DEFAULT_JAVA_VERSION)
                : DEFAULT_JAVA_VERSION;
    }

    public int maxSupportedJavaVersion() {
        return rawJavaVersions != null && rawJavaVersions.length == 2
                ? IntStream.of(rawJavaVersions).max().orElse(DEFAULT_JAVA_VERSION)
                : DEFAULT_JAVA_VERSION;
    }

    public @NotNull OptionalInt relevantJavaVersion() {
        int[] supportedJavaVersions = supportedJavaVersions().toArray();

        // firstly, iterate over LTS releases: 8, 11, 17, 21 and 25
        var relevantVersion = IntStream.of(supportedJavaVersions)
                .filter(JAVA_LTS_RELEASES::contains)
                .max();

        if (relevantVersion.isPresent())
            return relevantVersion;

        // fallback to min supported version
        return IntStream.of(supportedJavaVersions).min();
    }

    public int relevantJavaVersionOrDefault() {
        return relevantJavaVersion().orElse(DEFAULT_JAVA_VERSION);
    }

    public @NotNull IntStream supportedJavaVersions() {
        int minVersion = minSupportedJavaVersion();
        int maxVersion = maxSupportedJavaVersion();
        return IntStream.rangeClosed(minVersion, maxVersion).distinct();
    }

    public record Refs(
            @JsonProperty("BuildData") String buildData,
            @JsonProperty("Bukkit") String bukkit,
            @JsonProperty("CraftBukkit") String craftBukkit,
            @JsonProperty("Spigot") String spigot
    ) { }

}
