package io.github.soknight.gradle.buildtools;

import io.github.soknight.gradle.buildtools.extension.BuildToolsExtension;
import io.github.soknight.gradle.buildtools.service.BuildToolsService;
import io.github.soknight.gradle.buildtools.task.SetupBuildToolsTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.NotNull;

public final class BuildToolsPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        var extension = registerExtension(project);
        var service = registerBuildService(project, extension);
        registerTasks(project, extension, service);
    }

    private static void registerTasks(
            @NotNull Project project,
            @NotNull BuildToolsExtension extension,
            @NotNull Provider<BuildToolsService> service
    ) {
        project.getTasks().register("setupBuildTools", SetupBuildToolsTask.class, task -> {
            task.getBuildNumber().set(extension.getBuildNumber());
            task.getBuildNumber().finalizeValueOnRead();

            task.getOutputFile().set(service.flatMap(BuildToolsService::getBuildToolsJarFile));
            task.getOutputFile().finalizeValueOnRead();
        });
    }

    private static @NotNull Provider<BuildToolsService> registerBuildService(
            @NotNull Project project,
            @NotNull BuildToolsExtension extension
    ) {
        return project.getGradle().getSharedServices().registerIfAbsent("buildTools", BuildToolsService.class, spec -> {
            spec.getMaxParallelUsages().set(1);
            spec.getMaxParallelUsages().finalizeValue();

            spec.getParameters().getBuildRemappedJars().set(extension.getBuildRemappedJars());
            spec.getParameters().getWorkingDirectory().set(extension.getWorkingDirectory());
        });
    }

    private static @NotNull BuildToolsExtension registerExtension(@NotNull Project project) {
        var extension = project.getExtensions().create("buildTools", BuildToolsExtension.class);
        extension.getWorkingDirectory().convention(defaultWorkingDirectory(project));
        return extension;
    }

    private static @NotNull Provider<Directory> defaultWorkingDirectory(@NotNull Project project) {
        var workingDirectory = project.getGradle().getGradleUserHomeDir().toPath()
                .resolve("caches")
                .resolve("build-tools");

        return project.getLayout().dir(project.provider(workingDirectory::toFile));
    }

}
