package io.github.soknight.gradle.buildtools.service;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.jetbrains.annotations.NotNull;

public abstract class BuildToolsService implements BuildService<BuildToolsServiceParameters> {

    private static final @NotNull String BUILD_TOOLS_JAR_FILE_NAME = "BuildTools.jar";

    public BuildToolsService() {

    }

    public @NotNull Provider<RegularFile> getBuildToolsJarFile() {
        return getWorkingDirectory().file(BUILD_TOOLS_JAR_FILE_NAME);
    }

    public @NotNull DirectoryProperty getWorkingDirectory() {
        return getParameters().getWorkingDirectory();
    }

}
