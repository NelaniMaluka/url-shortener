package com.nelani.url_shortner.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents a shortened URL and its metadata.")
public record UrlResponse(

        @Schema(description = "Unique identifier of the URL entry", example = "d290f1ee-6c54-4b01-90e6-d701748f0851") UUID id,

        @Schema(description = "The original long URL provided by the user", example = "https://www.google.com/search?q=url+shortener") String originalUrl,

        @Schema(description = "The generated short URL", example = "https://sho.rt/a8f3Ks") String shortUrl,

        @Schema(description = "Timestamp when the short URL was created", example = "2025-12-06T14:23:00") LocalDateTime createdAt,

        @Schema(description = "Timestamp when the URL was last updated", example = "2025-12-06T14:45:00") LocalDateTime updatedAt,

        @Schema(description = "Timestamp when the URL will expire", example = "2025-12-06T14:50:00") LocalDateTime expiresAt,

        @Schema(description = "Number of times the short URL has been accessed", example = "42") Long clicks,

        @Schema(description = "Maximum number of unique devices allowed to access the short URL.", example = "5") Long accessLimit) {
}
