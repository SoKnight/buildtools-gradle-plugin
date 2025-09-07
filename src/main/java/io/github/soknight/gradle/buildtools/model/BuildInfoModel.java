package io.github.soknight.gradle.buildtools.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public record BuildInfoModel(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("refs") Map<String, String> gitRefs,
        @JsonProperty("hashes") Map<String, String> hashes,
        @JsonProperty("toolsVersion") Integer buildToolsVersion,
        @JsonProperty("javaVersions") List<Integer> supportedJavaVersions
) {

    public static final int DEFAULT_JAVA_VERSION = 52;  // Java 8

    public @NotNull OptionalInt relevantJavaVersion() {
        if (supportedJavaVersions == null || supportedJavaVersions.isEmpty())
            return OptionalInt.empty();

        // firstly, iterate over LTS releases: 8, 11, 17, 21 and 25
        var relevantVersion = IntStream.of(52, 55, 61, 65, 69)
                .filter(supportedJavaVersions::contains)
                .max();

        if (relevantVersion.isPresent())
            return relevantVersion;

        // fallback to minimal supported version
        return supportedJavaVersions.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).min();
    }

    public int relevantJavaVersionOrDefault() {
        return relevantJavaVersion().orElse(DEFAULT_JAVA_VERSION);
    }

}
