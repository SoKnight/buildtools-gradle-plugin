package io.github.soknight.gradle.buildtools.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonDeserializer {

    private static final @NotNull JsonMapper JSON_MAPPER = initializeJsonMapper();

    public static <T> @NotNull T deserializeJson(@NotNull Path filePath, @NotNull Class<T> type) throws IOException {
        try (var reader = newBufferedReader(filePath, UTF_8)) {
            return JSON_MAPPER.readValue(reader, type);
        }
    }

    private static @NotNull JsonMapper initializeJsonMapper() {
        return JsonMapper.builder()
                .enable(JsonParser.Feature.IGNORE_UNDEFINED)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

}
