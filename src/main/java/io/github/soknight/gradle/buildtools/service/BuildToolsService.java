package io.github.soknight.gradle.buildtools.service;

import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BuildToolsService implements BuildService<BuildToolsServiceParameters> {

    private static final @NotNull String BUILD_INFO_CACHE_DIRECTORY_NAME = "build-info-cache";
    private static final @NotNull String BUILD_RESULT_CACHE_DIRECTORY_NAME = "build-result-cache";
    private static final @NotNull String BUILD_TOOLS_JAR_FILE_NAME = "BuildTools.jar";

    private final @NotNull Lock buildToolsLock;

    public BuildToolsService() {
        this.buildToolsLock = new ReentrantLock();
    }

    @ApiStatus.Internal
    public void lockBuildTools() {
        this.buildToolsLock.lock();
    }

    @ApiStatus.Internal
    public void unlockBuildTools() {
        this.buildToolsLock.unlock();
    }

    public @NotNull Provider<Directory> getBuildInfoCacheDirectory() {
        return getWorkingDirectory().dir(BUILD_INFO_CACHE_DIRECTORY_NAME);
    }

    public @NotNull Provider<Directory> getBuildResultCacheDirectory() {
        return getWorkingDirectory().dir(BUILD_RESULT_CACHE_DIRECTORY_NAME);
    }

    public @NotNull Provider<RegularFile> getBuildToolsJarFile() {
        return getWorkingDirectory().file(BUILD_TOOLS_JAR_FILE_NAME);
    }

    public @NotNull DirectoryProperty getWorkingDirectory() {
        return getParameters().getWorkingDirectory();
    }

}
