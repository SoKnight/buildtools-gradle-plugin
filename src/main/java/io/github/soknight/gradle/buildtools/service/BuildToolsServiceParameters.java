package io.github.soknight.gradle.buildtools.service;

import io.github.soknight.gradle.buildtools.extension.ExpectedArtifactsDefinition;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildServiceParameters;
import org.jetbrains.annotations.NotNull;

public interface BuildToolsServiceParameters extends BuildServiceParameters {

    @NotNull Property<Boolean> getBuildRemappedJars();

    @NotNull Property<ExpectedArtifactsDefinition> getExpectedArtifacts();

    @NotNull DirectoryProperty getWorkingDirectory();

}
