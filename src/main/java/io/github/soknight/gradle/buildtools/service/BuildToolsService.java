package io.github.soknight.gradle.buildtools.service;

import io.github.soknight.gradle.buildtools.extension.ExpectedArtifactsDefinition;
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

    public @NotNull Provider<ExpectedArtifactsDefinition> getExpectedArtifacts() {
        return getParameters().getExpectedArtifacts();
    }

    public @NotNull Provider<RegularFile> getBuildToolsJarFile() {
        return getWorkingDirectory().file("BuildTools.jar");
    }

    public @NotNull Provider<Directory> getMetadataDirectory() {
        return getWorkingDirectory().dir("metadata");
    }

    public @NotNull Provider<RegularFile> getMetadataBuildInfoFile(@NotNull String minecraftVersion) {
        return getMetadataDirectory()
                .map(dir -> dir.dir("build-info"))
                .map(dir -> dir.file("%s.json".formatted(minecraftVersion)));
    }

    public @NotNull Provider<RegularFile> getMetadataVersionInfoFile(@NotNull String buildDataRef) {
        return getMetadataDirectory()
                .map(dir -> dir.dir("version-info"))
                .map(dir -> dir.file("%s.json".formatted(buildDataRef)));
    }

    public @NotNull Provider<Directory> getOutputDirectory() {
        return getWorkingDirectory().dir("out");
    }

    public @NotNull DirectoryProperty getWorkingDirectory() {
        return getParameters().getWorkingDirectory();
    }

}
