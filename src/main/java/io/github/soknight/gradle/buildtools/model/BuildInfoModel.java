package io.github.soknight.gradle.buildtools.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

public record BuildInfoModel(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("refs") Map<String, String> gitRefs,
        @JsonProperty("hashes") Map<String, String> hashes,
        @JsonProperty("toolsVersion") Integer buildToolsVersion,
        @JsonProperty("javaVersions") List<Integer> rawJavaVersionRange
) {

    public static final int DEFAULT_JAVA_VERSION = 52;  // Java 8

    // Java LTS releases: 8, 11, 17, 21 and 25
    public static final @NotNull Set<Integer> JAVA_LTS_RELEASES = Set.of(52, 55, 61, 65, 69);

    public int minSupportedJavaVersion() {
        return rawJavaVersionRange != null && rawJavaVersionRange.size() == 2
                ? rawJavaVersionRange.stream().mapToInt(Integer::intValue).min().orElse(DEFAULT_JAVA_VERSION)
                : DEFAULT_JAVA_VERSION;
    }

    public int maxSupportedJavaVersion() {
        return rawJavaVersionRange != null && rawJavaVersionRange.size() == 2
                ? rawJavaVersionRange.stream().mapToInt(Integer::intValue).max().orElse(DEFAULT_JAVA_VERSION)
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

}
