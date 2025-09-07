package io.github.soknight.gradle.buildtools.task;

import io.github.soknight.gradle.buildtools.service.BuildToolsService;
import io.github.soknight.gradle.buildtools.util.Downloader;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.ServiceReference;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

@CacheableTask
public abstract class SetupBuildToolsTask extends DefaultTask {

    private static final @NotNull String URL = "https://hub.spigotmc.org/jenkins/job/BuildTools/%d/artifact/target/BuildTools.jar";

    @TaskAction
    public void run() {
        var url = URL.formatted(getBuildNumber().get());
        var outputPath = getOutputFile().get().getAsFile().toPath();
        Downloader.download(url, outputPath);
    }

    @Input
    public abstract @NotNull Property<Integer> getBuildNumber();

    @OutputFile
    public abstract @NotNull RegularFileProperty getOutputFile();

    @ServiceReference("buildTools")
    public abstract @NotNull Property<BuildToolsService> getBuildToolsService();

}
