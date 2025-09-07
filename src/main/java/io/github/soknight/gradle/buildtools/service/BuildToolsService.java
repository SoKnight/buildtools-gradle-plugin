package io.github.soknight.gradle.buildtools.service;

import lombok.extern.slf4j.Slf4j;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.jetbrains.annotations.NotNull;

@Slf4j
public abstract class BuildToolsService implements BuildService<BuildToolsServiceParameters> {

    private static final @NotNull String BUILD_INFO_CACHE_DIRECTORY_NAME = "build-info-cache";
    private static final @NotNull String BUILD_TOOLS_JAR_FILE_NAME = "BuildTools.jar";

    public BuildToolsService() {

    }

    public @NotNull Provider<RegularFile> getBuildToolsJarFile() {
        return getWorkingDirectory().file(BUILD_TOOLS_JAR_FILE_NAME);
    }

    public @NotNull Provider<Directory> getBuildInfoCacheDirectory() {
        return getWorkingDirectory().dir(BUILD_INFO_CACHE_DIRECTORY_NAME);
    }

    public @NotNull DirectoryProperty getWorkingDirectory() {
        return getParameters().getWorkingDirectory();
    }

}
