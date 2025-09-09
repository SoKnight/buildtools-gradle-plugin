package io.github.soknight.gradle.buildtools.task;

import io.github.soknight.gradle.buildtools.extension.ExpectedArtifactsDefinition;
import io.github.soknight.gradle.buildtools.model.BuildInfoModel;
import io.github.soknight.gradle.buildtools.model.ExpectedArtifactType;
import io.github.soknight.gradle.buildtools.model.VersionInfoModel;
import io.github.soknight.gradle.buildtools.service.BuildToolsService;
import io.github.soknight.gradle.buildtools.util.JsonDeserializer;
import org.gradle.api.Action;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@CacheableTask
public abstract class BuildSpigotTask extends JavaExec {

    public BuildSpigotTask() {
        getBuildRemappedJars().convention(getBuildToolsService().get().getParameters().getBuildRemappedJars());
        getOutputFile().convention(getMinecraftVersion().map(this::resolveDefaultOutputFile));

        getJavaLauncher().set(getRequiredJavaVersion().flatMap(this::resolveJavaLauncher));
        getLogging().captureStandardOutput(LogLevel.INFO);
        getLogging().captureStandardError(LogLevel.ERROR);

        getOutputs().upToDateWhen(task -> isExpectedOutputFileExist());
        setOnlyIf(task -> !getSkipIfAllArtifactsExist().getOrElse(Boolean.TRUE) || !areAllExpectedArtifactsExist());

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

    public synchronized void expectedArtifacts(@NotNull Action<ExpectedArtifactsDefinition> action) {
        var expectedArtifacts = getExpectedArtifacts().getOrNull();
        if (expectedArtifacts == null) {
            expectedArtifacts = getProject().getObjects().newInstance(ExpectedArtifactsDefinition.class);
            getExpectedArtifacts().set(expectedArtifacts);
        }

        action.execute(expectedArtifacts);
    }

    public void useFetchBuildInfoTask(@NotNull String taskName) {
        useFetchBuildInfoTask(getProject().getTasks().named(taskName, FetchBuildInfoTask.class));
    }

    public void useFetchBuildInfoTask(@NotNull FetchBuildInfoTask task) {
        getMinecraftVersion().set(task.getMinecraftVersion());
        getRequiredJavaVersion().set(task.getModel().map(BuildInfoModel::relevantJavaVersionOrDefault));
        dependsOn(task);
    }

    public void useFetchBuildInfoTask(@NotNull Provider<FetchBuildInfoTask> taskProvider) {
        var buildInfoProvider = taskProvider.flatMap(FetchBuildInfoTask::getModel);
        getMinecraftVersion().set(taskProvider.flatMap(FetchBuildInfoTask::getMinecraftVersion));
        getRequiredJavaVersion().set(buildInfoProvider.map(BuildInfoModel::relevantJavaVersionOrDefault));
        dependsOn(taskProvider);
    }

    public void useFetchVersionInfoTask(@NotNull String taskName) {
        useFetchVersionInfoTask(getProject().getTasks().named(taskName, FetchVersionInfoTask.class));
    }

    public void useFetchVersionInfoTask(@NotNull FetchVersionInfoTask task) {
        getMinecraftVersion().set(task.getModel().map(VersionInfoModel::minecraftVersion));
        getSpigotVersion().set(task.getModel().map(VersionInfoModel::spigotVersion));
        dependsOn(task);
    }

    public void useFetchVersionInfoTask(@NotNull Provider<FetchVersionInfoTask> taskProvider) {
        var versionInfoProvider = taskProvider.flatMap(FetchVersionInfoTask::getModel);
        getMinecraftVersion().set(versionInfoProvider.map(VersionInfoModel::minecraftVersion));
        getSpigotVersion().set(versionInfoProvider.map(VersionInfoModel::spigotVersion));
        dependsOn(taskProvider);
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

    private boolean isExpectedOutputFileExist() {
        if (isExpectedArtifact(def -> def.getBootstrapJar().getOrNull())) {
            var outputFile = getOutputFile().getAsFile().get();
            if (outputFile.isFile() && outputFile.length() > 0L) {
                getLogger().info("BOOTSTRAP_JAR > UP-TO-DATE: '{}'", outputFile.getAbsolutePath());
            } else {
                getLogger().warn("BOOTSTRAP_JAR > OUTDATED: '{}'", outputFile.getAbsolutePath());
                return false;
            }
        }

        return true;
    }

    private boolean areAllExpectedArtifactsExist() {
        if (!isExpectedOutputFileExist())
            return false;

        var minecraftSnapshot = getSpigotVersion().orElse(getMinecraftVersion().map("%s-SNAPSHOT"::formatted)).get();
        var spigotSnapshot = getSpigotVersion().orElse(getMinecraftVersion().map("%s-R0.1-SNAPSHOT"::formatted)).get();

        var localMavenRepoPath = Path.of(System.getProperty("user.home"), ".m2", "repository");
        var supportsRemapped = getMinecraftVersion().map(this::hasMappingsUrl).getOrElse(Boolean.FALSE);

        for (var artifactType : ExpectedArtifactType.values()) {
            if (artifactType.requiresBuildWithRemappedOption() && !supportsRemapped)
                continue;

            var filePath = localMavenRepoPath.resolve(artifactType.repoPath(minecraftSnapshot, spigotSnapshot));
            if (Files.isRegularFile(filePath) && filePath.toFile().length() > 0L) {
                getLogger().info("{} > UP-TO-DATE: '{}'", artifactType, filePath);
            } else {
                getLogger().warn("{} > OUTDATED: '{}'", artifactType, filePath);
                return false;
            }
        }

        return true;
    }

    private boolean isExpectedArtifact(@NotNull Function<ExpectedArtifactsDefinition, Boolean> valueFunction) {
        var taskScoped = getExpectedArtifacts().map(valueFunction::apply).getOrNull();
        if (taskScoped != null)
            return taskScoped;

        var globalScoped = getBuildToolsService()
                .flatMap(BuildToolsService::getExpectedArtifacts)
                .map(valueFunction::apply)
                .getOrNull();

        return globalScoped != null ? globalScoped : true;
    }

    private @NotNull List<String> constructArgs(@NotNull Path outputPath) {
        var produceRemapped = getBuildRemappedJars()
                .orElse(getMinecraftVersion().map(this::hasMappingsUrl))
                .getOrElse(Boolean.FALSE);

        List<String> args = new ArrayList<>();
        args.add("--nogui");

        args.add("--final-name");
        args.add(outputPath.getFileName().toString());

        args.add("--output-dir");
        args.add(outputPath.getParent().toAbsolutePath().toString());

        if (produceRemapped)
            args.add("--remapped");

        args.add("--rev");
        args.add(getMinecraftVersion().get());
        return args;
    }

    private @Nullable Boolean hasMappingsUrl(@NotNull String minecraftVersion) {
        var buildInfoFile = getBuildToolsService().get().getMetadataBuildInfoFile(minecraftVersion);
        var buildInfoModel = deserializeModel(buildInfoFile, BuildInfoModel.class);
        if (buildInfoModel == null)
            return null;

        var buildDataRef = buildInfoModel.gitRefs().buildData();
        var versionInfoFile = getBuildToolsService().get().getMetadataVersionInfoFile(buildDataRef);
        var versionInfoModel = deserializeModel(versionInfoFile, VersionInfoModel.class);
        if (versionInfoModel == null)
            return null;

        return versionInfoModel.mappingsUrl() != null;
    }

    private <T> @Nullable T deserializeModel(
            @NotNull Provider<RegularFile> fileProvider,
            @NotNull Class<T> type
    ) {
        try {
            var filePath = fileProvider.get().getAsFile().toPath();
            return JsonDeserializer.deserializeJson(filePath, type);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Input @Optional
    public abstract @NotNull Property<Boolean> getBuildRemappedJars();

    @Input @Optional
    public abstract @NotNull Property<ExpectedArtifactsDefinition> getExpectedArtifacts();

    @Input @Optional
    public abstract @NotNull Property<String> getMinecraftVersion();

    @Input @Optional
    public abstract @NotNull Property<String> getSpigotVersion();

    @Input @Optional
    public abstract @NotNull Property<Boolean> getSkipIfAllArtifactsExist();

    @Input
    public abstract @NotNull Property<Integer> getRequiredJavaVersion();

    @OutputFile
    public abstract @NotNull RegularFileProperty getOutputFile();

    @ServiceReference("buildTools")
    public abstract @NotNull Property<BuildToolsService> getBuildToolsService();

}
