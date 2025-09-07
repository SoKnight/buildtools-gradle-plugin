package io.github.soknight.gradle.buildtools.task;

import io.github.soknight.gradle.buildtools.service.BuildToolsService;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.ServiceReference;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

import static java.net.http.HttpClient.Redirect.ALWAYS;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;

@CacheableTask
public abstract class SetupBuildToolsTask extends DefaultTask {

    private static final @NotNull Duration CONNECT_TIMEOUT = Duration.ofSeconds(5L);
    private static final @NotNull Duration REQUEST_TIMEOUT = Duration.ofSeconds(10L);
    private static final @NotNull String USER_AGENT = "BuildTools Gradle Plugin";

    private static final @NotNull HttpClient HTTP_CLIENT = initializeHttpClient();

    private static final @NotNull String DOWNLOAD_URL = "https://hub.spigotmc.org/jenkins/job/BuildTools/%d/artifact/target/BuildTools.jar";

    @TaskAction
    public void run() {
        var buildToolsService = getBuildToolsService().get();
        var buildNumber = getBuildNumber().get();

        var request = HttpRequest.newBuilder(URI.create(DOWNLOAD_URL.formatted(buildNumber))).GET()
                .setHeader("User-Agent", USER_AGENT)
                .timeout(REQUEST_TIMEOUT)
                .build();

        getLogger().info("Sending GET request to '{}'...", request.uri());
        try {
            var response = HTTP_CLIENT.send(request, ofInputStream());
            if (response.statusCode() / 100 != 2) {
                getLogger().error("Got response code: #{}", response.statusCode());
                throw new GradleException("Couldn't download BuildTools JAR artifact!");
            }

            var filePath = buildToolsService.getBuildToolsJarFile().get().getAsFile().toPath();
            try (
                    var bodyStream = response.body();
                    var fileStream = newOutputStream(filePath, CREATE, TRUNCATE_EXISTING)
            ) {
                bodyStream.transferTo(fileStream);
                fileStream.flush();
                getLogger().info("BuildTools JAR artifact stored at: '{}'", filePath);
            }
        } catch (IOException ex) {
            throw new GradleException("Couldn't download BuildTools JAR artifact!", ex);
        } catch (InterruptedException ignored) {
        }
    }

    @Input
    public abstract @NotNull Property<Integer> getBuildNumber();

    @OutputFile
    public abstract @NotNull RegularFileProperty getOutputFile();

    @ServiceReference("buildTools")
    public abstract @NotNull Property<BuildToolsService> getBuildToolsService();

    private static @NotNull HttpClient initializeHttpClient() {
        return HttpClient.newBuilder()
                .executor(newVirtualThreadPerTaskExecutor())
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(ALWAYS)
                .build();
    }

}
