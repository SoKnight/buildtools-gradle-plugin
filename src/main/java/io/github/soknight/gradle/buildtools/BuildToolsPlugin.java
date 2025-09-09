package io.github.soknight.gradle.buildtools;

import io.github.soknight.gradle.buildtools.extension.BuildToolsExtension;
import io.github.soknight.gradle.buildtools.service.BuildToolsService;
import io.github.soknight.gradle.buildtools.task.BuildSpigotTask;
import io.github.soknight.gradle.buildtools.task.FetchBuildInfoTask;
import io.github.soknight.gradle.buildtools.task.FetchVersionInfoTask;
import io.github.soknight.gradle.buildtools.task.SetupBuildToolsTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.NotNull;

public final class BuildToolsPlugin implements Plugin<Project> {

    private static final @NotNull String TASK_GROUP_NAME = "buildtools";

    @Override
    public void apply(@NotNull Project project) {
        var extension = registerExtension(project);
        var service = registerBuildService(project, extension);
        registerTasks(project, extension, service);

        project.afterEvaluate(this::afterEvaluate);
    }

    private void afterEvaluate(@NotNull Project project) {
        var extension = project.getExtensions().getByType(BuildToolsExtension.class);
        var buildVersions = extension.getMinecraftVersions().getOrNull();
        if (buildVersions == null || buildVersions.isEmpty())
            return;

        var tasks = project.getTasks();
        var setupBuildToolsTask = project.getTasks().named("setupBuildTools", SetupBuildToolsTask.class);

        var buildSpigotTasks = buildVersions.stream().distinct().map(buildVersion -> {
            var fetchBuildInfoTaskName = "fetchBuildInfo@%s".formatted(buildVersion);
            var fetchBuildInfoTask = tasks.register(fetchBuildInfoTaskName, FetchBuildInfoTask.class, task -> {
                task.getMinecraftVersion().set(buildVersion);
                task.setGroup(TASK_GROUP_NAME);

                task.getMinecraftVersion().finalizeValueOnRead();
                task.getModel().finalizeValueOnRead();
                task.getOutputFile().finalizeValueOnRead();
            });

            var fetchVersionInfoTaskName = "fetchVersionInfo@%s".formatted(buildVersion);
            var fetchVersionInfoTask = tasks.register(fetchVersionInfoTaskName, FetchVersionInfoTask.class, task -> {
                task.setGroup(TASK_GROUP_NAME);
                task.useFetchBuildInfoTask(fetchBuildInfoTask);

                task.getBuildDataRef().finalizeValueOnRead();
                task.getModel().finalizeValueOnRead();
                task.getOutputFile().finalizeValueOnRead();
            });

            var buildSpigotTaskName = "buildSpigot@%s".formatted(buildVersion);
            return tasks.register(buildSpigotTaskName, BuildSpigotTask.class, task -> {
                task.setGroup(TASK_GROUP_NAME);
                task.useFetchBuildInfoTask(fetchBuildInfoTask);
                task.useFetchVersionInfoTask(fetchVersionInfoTask);

                task.getBuildRemappedJars().finalizeValueOnRead();
                task.getExpectedArtifacts().finalizeValueOnRead();
                task.getMinecraftVersion().finalizeValueOnRead();
                task.getOutputFile().finalizeValueOnRead();
                task.getRequiredJavaVersion().finalizeValueOnRead();
                task.getSkipIfAllArtifactsExist().finalizeValueOnRead();
                task.getSpigotVersion().finalizeValueOnRead();
            });
        }).toList();

        tasks.register("buildAllSpigot", task -> {
            task.setGroup(TASK_GROUP_NAME);
            buildSpigotTasks.forEach(task::dependsOn);
        });
    }

    private static void registerTasks(
            @NotNull Project project,
            @NotNull BuildToolsExtension extension,
            @NotNull Provider<BuildToolsService> service
    ) {
        project.getTasks().register("setupBuildTools", SetupBuildToolsTask.class, task -> {
            task.setGroup(TASK_GROUP_NAME);

            task.getBuildToolsVersion().set(extension.getBuildToolsVersion());
            task.getBuildToolsVersion().finalizeValueOnRead();

            task.getOutputFile().set(service.flatMap(BuildToolsService::getBuildToolsJarFile));
            task.getOutputFile().finalizeValueOnRead();
        });
    }

    private static @NotNull Provider<BuildToolsService> registerBuildService(
            @NotNull Project project,
            @NotNull BuildToolsExtension extension
    ) {
        return project.getGradle().getSharedServices().registerIfAbsent("buildTools", BuildToolsService.class, spec -> {
            spec.getParameters().getBuildRemappedJars().set(extension.getBuildRemappedJars());
            spec.getParameters().getExpectedArtifacts().set(extension.getExpectedArtifacts());
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
