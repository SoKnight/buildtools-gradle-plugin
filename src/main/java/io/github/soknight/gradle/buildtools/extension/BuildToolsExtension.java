package io.github.soknight.gradle.buildtools.extension;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

public abstract class BuildToolsExtension {

    private final @NotNull ObjectFactory objectFactory;

    @Inject
    public BuildToolsExtension(@NotNull ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public synchronized void expectedArtifacts(@NotNull Action<ExpectedArtifactsDefinition> action) {
        var expectedArtifacts = getExpectedArtifacts().getOrNull();
        if (expectedArtifacts == null) {
            expectedArtifacts = objectFactory.newInstance(ExpectedArtifactsDefinition.class);
            getExpectedArtifacts().set(expectedArtifacts);
        }

        action.execute(expectedArtifacts);
    }

    public abstract @NotNull Property<Integer> getBuildToolsVersion();

    public abstract @NotNull Property<Boolean> getBuildRemappedJars();

    public abstract @NotNull Property<ExpectedArtifactsDefinition> getExpectedArtifacts();

    public abstract @NotNull ListProperty<String> getMinecraftVersions();

    public abstract @NotNull DirectoryProperty getWorkingDirectory();

}
