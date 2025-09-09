package io.github.soknight.gradle.buildtools.task;

import io.github.soknight.gradle.buildtools.model.BuildInfoModel;
import io.github.soknight.gradle.buildtools.model.VersionInfoModel;
import io.github.soknight.gradle.buildtools.service.BuildToolsService;
import org.gradle.api.JavaVersion;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.ServiceReference;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@DisableCachingByDefault(because = "Writes to the local Maven repo; not safe for build cache")
public abstract class BuildSpigotTask extends JavaExec {

    public BuildSpigotTask() {
        getBuildRemappedJars().convention(getBuildToolsService().get().getParameters().getBuildRemappedJars());
        getSpigotVersion().set(getMinecraftVersion().map(version -> version + "-SNAPSHOT"));
        getOutputFile().set(getMinecraftVersion().map(this::resolveDefaultOutputFile));

        getJavaLauncher().set(getRequiredJavaVersion().flatMap(this::resolveJavaLauncher));
        getLogging().captureStandardOutput(LogLevel.INFO);
        getLogging().captureStandardError(LogLevel.ERROR);

        dependsOn(getProject().getTasks().named("setupBuildTools", SetupBuildToolsTask.class));
    }

    @Override
    public void exec() {
        var buildToolsService = getBuildToolsService().get();
        args(constructArgs(getOutputFile().get().getAsFile().toPath()));
        classpath(getProject().files(buildToolsService.getBuildToolsJarFile()));
        workingDir(buildToolsService.getWorkingDirectory());

        buildToolsService.lockBuildTools();
        try {
            super.exec();
        } finally {
            buildToolsService.unlockBuildTools();
        }
    }

    public void useFetchBuildInfoTask(@NotNull String taskName) {
        useFetchBuildInfoTask(getProject().getTasks().named(taskName, FetchBuildInfoTask.class));
    }

    public void useFetchBuildInfoTask(@NotNull FetchBuildInfoTask task) {
        getMinecraftVersion().convention(task.getMinecraftVersion());
        getRequiredJavaVersion().convention(task.getModel().map(BuildInfoModel::relevantJavaVersionOrDefault));
        dependsOn(task);
    }

    public void useFetchBuildInfoTask(@NotNull Provider<FetchBuildInfoTask> taskProvider) {
        var buildInfoProvider = taskProvider.flatMap(FetchBuildInfoTask::getModel);
        getMinecraftVersion().convention(taskProvider.flatMap(FetchBuildInfoTask::getMinecraftVersion));
        getRequiredJavaVersion().convention(buildInfoProvider.map(BuildInfoModel::relevantJavaVersionOrDefault));
        dependsOn(taskProvider);
    }

    public void useFetchVersionInfoTask(@NotNull String taskName) {
        useFetchVersionInfoTask(getProject().getTasks().named(taskName, FetchVersionInfoTask.class));
    }

    public void useFetchVersionInfoTask(@NotNull FetchVersionInfoTask task) {
        getMinecraftVersion().convention(task.getModel().map(VersionInfoModel::minecraftVersion));
        getSpigotVersion().convention(task.getModel().map(VersionInfoModel::spigotVersion));
        dependsOn(task);
    }

    public void useFetchVersionInfoTask(@NotNull Provider<FetchVersionInfoTask> taskProvider) {
        var versionInfoProvider = taskProvider.flatMap(FetchVersionInfoTask::getModel);
        getMinecraftVersion().convention(versionInfoProvider.map(VersionInfoModel::minecraftVersion));
        getSpigotVersion().convention(versionInfoProvider.map(VersionInfoModel::spigotVersion));
        dependsOn(taskProvider);
    }

    private @NotNull List<String> constructArgs(@NotNull Path outputPath) {
        List<String> args = new ArrayList<>();
        args.add("--nogui");

        args.add("--final-name");
        args.add(outputPath.getFileName().toString());

        args.add("--output-dir");
        args.add(outputPath.getParent().toAbsolutePath().toString());

        if (getBuildRemappedJars().getOrElse(Boolean.TRUE))
            args.add("--remapped");

        args.add("--rev");
        args.add(getMinecraftVersion().get());
        return args;
    }

    private @Nullable Provider<JavaLauncher> resolveJavaLauncher(@Nullable Integer requiredJavaVersion) {
        if (requiredJavaVersion == null || requiredJavaVersion <= 0)
            return null;

        var javaToolchainService = getProject().getExtensions().getByType(JavaToolchainService.class);
        var javaMajorVersion = JavaVersion.forClassVersion(requiredJavaVersion).getMajorVersion();
        var javaLanguageVersion = JavaLanguageVersion.of(javaMajorVersion);
        return javaToolchainService.launcherFor(spec -> spec.getLanguageVersion().set(javaLanguageVersion));
    }

    private @NotNull RegularFile resolveDefaultOutputFile(@NotNull String minecraftVersion) {
        var directory = getBuildToolsService().flatMap(BuildToolsService::getOutputDirectory).get();
        return directory.file("spigot-%s.jar".formatted(minecraftVersion));
    }

    @Input @Optional
    public abstract @NotNull Property<Boolean> getBuildRemappedJars();

    @Input @Optional
    public abstract @NotNull Property<String> getMinecraftVersion();

    @Input @Optional
    public abstract @NotNull Property<String> getSpigotVersion();

    @Input
    public abstract @NotNull Property<Integer> getRequiredJavaVersion();

    @OutputFile
    public abstract @NotNull RegularFileProperty getOutputFile();

    @ServiceReference("buildTools")
    public abstract @NotNull Property<BuildToolsService> getBuildToolsService();

}
