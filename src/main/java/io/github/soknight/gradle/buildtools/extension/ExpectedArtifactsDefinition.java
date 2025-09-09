package io.github.soknight.gradle.buildtools.extension;

import io.github.soknight.gradle.buildtools.model.ExpectedArtifactType;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

public abstract class ExpectedArtifactsDefinition {

    public abstract @NotNull Property<Boolean> getBootstrapJar();

    public abstract @NotNull MapProperty<ExpectedArtifactType, Boolean> getByType();

}
