package io.github.soknight.gradle.buildtools.extension;

import io.github.soknight.gradle.buildtools.model.ExpectedArtifactType;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

public abstract class ExpectedArtifactsDefinition {

    public void expect(@NotNull ExpectedArtifactType type, boolean isExpected) {
        getByType().put(type, isExpected);
    }

    public void bootstrapJar(boolean isExpected) {
        getBootstrapJar().set(isExpected);
    }

    public void mappingsMojang(boolean isExpected) {
        expect(ExpectedArtifactType.MAPPINGS_MOJANG, isExpected);
    }

    public void mappingsSpigot(boolean isExpected) {
        expect(ExpectedArtifactType.MAPPINGS_SPIGOT, isExpected);
    }

    public void minecraftServerJar(boolean isExpected) {
        expect(ExpectedArtifactType.MINECRAFT_SERVER_JAR, isExpected);
    }

    public void spigotApiJar(boolean isExpected) {
        expect(ExpectedArtifactType.SPIGOT_API_JAR, isExpected);
    }

    public void spigotJar(boolean isExpected) {
        expect(ExpectedArtifactType.SPIGOT_JAR, isExpected);
    }

    public void spigotRemappedMojangJar(boolean isExpected) {
        expect(ExpectedArtifactType.SPIGOT_REMAPPED_MOJANG_JAR, isExpected);
    }

    public void spigotRemappedObfJar(boolean isExpected) {
        expect(ExpectedArtifactType.SPIGOT_REMAPPED_OBF_JAR, isExpected);
    }

    public abstract @NotNull Property<Boolean> getBootstrapJar();

    public abstract @NotNull MapProperty<ExpectedArtifactType, Boolean> getByType();

}
