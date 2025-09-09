package io.github.soknight.gradle.buildtools.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VersionInfoModel(
        @JsonProperty("accessTransforms") String accessTransforms,
        @JsonProperty("classMapCommand") String classMapCommand,
        @JsonProperty("classMappings") String classMappings,
        @JsonProperty("decompileCommand") String decompileCommand,
        @JsonProperty("finalMapCommand") String finalMapCommand,
        @JsonProperty("mappingsUrl") String mappingsUrl,
        @JsonProperty("memberMapCommand") String memberMapCommand,
        @JsonProperty("memberMappings") String memberMappings,
        @JsonProperty("minecraftHash") String minecraftHash,
        @JsonProperty("minecraftVersion") String minecraftVersion,
        @JsonProperty("packageMappings") String packageMappings,
        @JsonProperty("serverUrl") String serverUrl,
        @JsonProperty("spigotVersion") String spigotVersion,
        @JsonProperty("toolsVersion") Integer buildToolsVersion
) {

}
