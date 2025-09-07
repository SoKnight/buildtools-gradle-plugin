package io.github.soknight.gradle.buildtools.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.GradleException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import static io.github.soknight.gradle.buildtools.Constants.USER_AGENT;
import static java.net.http.HttpClient.Redirect.ALWAYS;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Downloader {

    private static final @NotNull Duration CONNECT_TIMEOUT = Duration.ofSeconds(5L);
    private static final @NotNull Duration REQUEST_TIMEOUT = Duration.ofSeconds(10L);

    private static final @NotNull HttpClient HTTP_CLIENT = initializeHttpClient();

    public static void download(@NotNull String url, @NotNull Path outputPath) {
        var request = HttpRequest.newBuilder(URI.create(url)).GET()
                .setHeader("User-Agent", USER_AGENT)
                .timeout(REQUEST_TIMEOUT)
                .build();

        try {
            log.info("Sending GET request to '{}'...", request.uri());
            var response = HTTP_CLIENT.send(request, ofInputStream());
            if (response.statusCode() / 100 != 2)
                throw new IOException("Server returned status code #%d".formatted(response.statusCode()));

            try (var bodyStream = response.body()) {
                Files.createDirectories(outputPath.getParent());
                Files.copy(bodyStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Downloaded '{}' from: '{}'", outputPath, response.uri());
            } catch (IOException ex) {
                throw new GradleException("Couldn't download '%s' from: '%s'".formatted(outputPath, request.uri()), ex);
            }
        } catch (IOException ex) {
            throw new GradleException("Couldn't execute GET request to: '%s'".formatted(request.uri()), ex);
        } catch (InterruptedException ignored) {
        }
    }

    private static @NotNull HttpClient initializeHttpClient() {
        return HttpClient.newBuilder()
                .executor(newVirtualThreadPerTaskExecutor())
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(ALWAYS)
                .build();
    }

}
