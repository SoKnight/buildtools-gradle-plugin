package io.github.soknight.gradle.buildtools.task;

import io.github.soknight.gradle.buildtools.model.BuildInfoModel;
import io.github.soknight.gradle.buildtools.service.BuildToolsService;
import org.gradle.api.JavaVersion;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.ServiceReference;
import org.gradle.api.tasks.*;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.github.soknight.gradle.buildtools.Constants.DEFAULT_BUILD_VERSION;

@CacheableTask
public abstract class BuildSpigotTask extends JavaExec {

    public BuildSpigotTask() {
        getBuildRemappedJars().set(getBuildToolsService().get().getParameters().getBuildRemappedJars());
        getBuildVersion().set(DEFAULT_BUILD_VERSION);
        getOutputFile().set(getBuildVersion().map(this::resolveDefaultOutputFile));

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
        getBuildVersion().set(task.getBuildVersion());
        getRequiredJavaVersion().set(task.getBuildInfo().map(BuildInfoModel::relevantJavaVersionOrDefault));
        dependsOn(task);
    }

    public void useFetchBuildInfoTask(@NotNull Provider<FetchBuildInfoTask> taskProvider) {
        var buildInfoProvider = taskProvider.flatMap(FetchBuildInfoTask::getBuildInfo);
        getBuildVersion().set(taskProvider.flatMap(FetchBuildInfoTask::getBuildVersion));
        getRequiredJavaVersion().set(buildInfoProvider.map(BuildInfoModel::relevantJavaVersionOrDefault));
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
        args.add(getBuildVersion().get());
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

    private @NotNull RegularFile resolveDefaultOutputFile(@NotNull String buildVersion) {
        var directory = getBuildToolsService().flatMap(BuildToolsService::getBuildResultCacheDirectory).get();
        return directory.file("spigot-%s.jar".formatted(buildVersion));
    }

    @Input @Optional
    public abstract @NotNull Property<Boolean> getBuildRemappedJars();

    @Input @Optional
    public abstract @NotNull Property<String> getBuildVersion();

    @Input
    public abstract @NotNull Property<Integer> getRequiredJavaVersion();

    @OutputFile
    public abstract @NotNull RegularFileProperty getOutputFile();

    @ServiceReference("buildTools")
    public abstract @NotNull Property<BuildToolsService> getBuildToolsService();

}
