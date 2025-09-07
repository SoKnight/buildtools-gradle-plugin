package io.github.soknight.gradle.buildtools.task;

import io.github.soknight.gradle.buildtools.model.BuildInfoModel;
import io.github.soknight.gradle.buildtools.service.BuildToolsService;
import io.github.soknight.gradle.buildtools.util.Downloader;
import io.github.soknight.gradle.buildtools.util.JsonDeserializer;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.ServiceReference;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@CacheableTask
public abstract class FetchBuildInfoTask extends DefaultTask {

    private static final @NotNull String URL = "https://hub.spigotmc.org/versions/%s.json";

    public FetchBuildInfoTask() {
        getBuildInfo().set(getOutputFile().map(this::deserializeBuildInfo));
    }

    @TaskAction
    public void run() {
        var url = URL.formatted(getBuildVersion().get());
        var outputPath = getOutputFile().get().getAsFile().toPath();
        Downloader.download(url, outputPath);
    }

    @Input
    public abstract @NotNull Property<String> getBuildVersion();

    @Internal
    public abstract @NotNull Property<BuildInfoModel> getBuildInfo();

    @OutputFile
    public abstract @NotNull RegularFileProperty getOutputFile();

    @ServiceReference("buildTools")
    public abstract @NotNull Property<BuildToolsService> getBuildToolsService();

    private @NotNull BuildInfoModel deserializeBuildInfo(@NotNull RegularFile file) {
        var filePath = file.getAsFile().toPath();
        try {
            var model = JsonDeserializer.deserializeJson(filePath, BuildInfoModel.class);
            getLogger().info("BuildInfoModel from '{}': {}", filePath, model);
            return model;
        } catch (IOException ex) {
            throw new GradleException("Couldn't deserialize BuildInfoModel from: '%s'".formatted(filePath), ex);
        }
    }

}
