package io.github.soknight.gradle.buildtools.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public record BuildInfoModel(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("refs") Map<String, String> gitRefs,
        @JsonProperty("hashes") Map<String, String> hashes,
        @JsonProperty("toolsVersion") Integer buildToolsVersion,
        @JsonProperty("javaVersions") List<Integer> supportedJavaVersions
) {

    public static final int DEFAULT_JAVA_VERSION = 52;  // Java 8

    public @NotNull OptionalInt minimalJavaVersion() {
        return supportedJavaVersions != null
                ? supportedJavaVersions.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).min()
                : OptionalInt.empty();
    }

    public int minimalJavaVersionOrDefault() {
        return minimalJavaVersion().orElse(DEFAULT_JAVA_VERSION);
    }

}
