package io.github.soknight.gradle.buildtools.task;

import io.github.soknight.gradle.buildtools.model.BuildInfoModel;
import io.github.soknight.gradle.buildtools.model.BuildInfoModel.Refs;
import io.github.soknight.gradle.buildtools.model.VersionInfoModel;
import io.github.soknight.gradle.buildtools.service.BuildToolsService;
import io.github.soknight.gradle.buildtools.util.Downloader;
import io.github.soknight.gradle.buildtools.util.JsonDeserializer;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.ServiceReference;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@CacheableTask
public abstract class FetchVersionInfoTask extends DefaultTask {

    private static final @NotNull String URL = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/info.json?at=%s";

    public FetchVersionInfoTask() {
        getModel().set(getOutputFile().map(this::deserializeModel));
        getOutputFile().set(getBuildDataRef().flatMap(getBuildToolsService().get()::getMetadataVersionInfoFile));
    }

    @TaskAction
    public void run() {
        var url = URL.formatted(URLEncoder.encode(getBuildDataRef().get(), StandardCharsets.UTF_8));
        var outputPath = getOutputFile().get().getAsFile().toPath();
        Downloader.download(url, outputPath);
    }

    public void useFetchBuildInfoTask(@NotNull String taskName) {
        useFetchBuildInfoTask(getProject().getTasks().named(taskName, FetchBuildInfoTask.class));
    }

    public void useFetchBuildInfoTask(@NotNull FetchBuildInfoTask task) {
        getBuildDataRef().set(task.getModel().map(BuildInfoModel::gitRefs).map(Refs::buildData));
        dependsOn(task);
    }

    public void useFetchBuildInfoTask(@NotNull Provider<FetchBuildInfoTask> taskProvider) {
        var buildInfoProvider = taskProvider.flatMap(FetchBuildInfoTask::getModel);
        getBuildDataRef().set(buildInfoProvider.map(BuildInfoModel::gitRefs).map(Refs::buildData));
        dependsOn(taskProvider);
    }

    private @NotNull VersionInfoModel deserializeModel(@NotNull RegularFile file) {
        var filePath = file.getAsFile().toPath();
        try {
            var model = JsonDeserializer.deserializeJson(filePath, VersionInfoModel.class);
            getLogger().info("VersionInfoModel from '{}': {}", filePath, model);
            return model;
        } catch (IOException ex) {
            throw new GradleException("Couldn't deserialize VersionInfoModel from: '%s'".formatted(filePath), ex);
        }
    }

    @Internal
    public abstract @NotNull Property<VersionInfoModel> getModel();

    @Input @Optional
    public abstract @NotNull Property<String> getBuildDataRef();

    @OutputFile
    public abstract @NotNull RegularFileProperty getOutputFile();

    @ServiceReference("buildTools")
    public abstract @NotNull Property<BuildToolsService> getBuildToolsService();

}
