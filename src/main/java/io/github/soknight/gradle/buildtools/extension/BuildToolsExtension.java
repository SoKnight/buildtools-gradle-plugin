package io.github.soknight.gradle.buildtools.extension;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

public abstract class BuildToolsExtension {

    public abstract @NotNull Property<Integer> getBuildNumber();

    public abstract @NotNull Property<Boolean> getBuildRemappedJars();

    public abstract @NotNull DirectoryProperty getWorkingDirectory();

}
